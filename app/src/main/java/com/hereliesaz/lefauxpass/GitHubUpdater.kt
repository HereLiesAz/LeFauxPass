package com.hereliesaz.lefauxpass

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.time.Instant
import java.util.Locale

object GitHubUpdater {
    private const val RELEASES_API_URL =
        "https://api.github.com/repos/HereLiesAz/LeFauxPass/releases?per_page=20"
    private const val USER_AGENT = "LeFauxPass-Android-Updater"
    private const val API_VERSION = "2022-11-28"
    private const val PREFS_NAME = "github_release_updater"
    private const val KEY_LAST_CHECK_MS = "last_check_ms"
    private const val KEY_WAITING_FOR_INSTALL_PERMISSION = "waiting_for_install_permission"
    private const val UPDATE_FILE_NAME = "lefauxpass-update.apk"
    private const val AUTO_CHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L

    private val updateMutex = Mutex()

    private data class ReleaseAsset(
        val name: String,
        val downloadUrl: String,
        val digest: String?,
        val createdAt: Instant,
        val buildNumberHint: Long?
    )

    suspend fun checkForUpdates(
        context: Context,
        showToastIfUpToDate: Boolean = false,
        force: Boolean = false
    ) {
        updateMutex.withLock {
            val appContext = context.applicationContext

            if (!force && !isAutoCheckDue(appContext)) {
                return
            }

            markChecked(appContext)

            if (force) {
                showToast(appContext, "Checking GitHub releases...")
            }

            try {
                val currentVersionCode = installedVersionCode(appContext)
                val candidate = findUpdateCandidate(currentVersionCode)

                if (candidate == null) {
                    if (showToastIfUpToDate) {
                        showToast(appContext, "LeFauxPass is up to date.")
                    }
                    return
                }

                showToast(appContext, "Downloading update ${candidate.name}...", long = true)

                val updateFile = downloadUpdate(appContext, candidate)
                val archiveInfo = getArchivePackageInfo(appContext, updateFile)
                    ?: throw IllegalStateException("Downloaded APK could not be read.")

                if (archiveInfo.packageName != appContext.packageName) {
                    updateFile.delete()
                    throw SecurityException("Downloaded APK has the wrong package name.")
                }

                val archiveVersionCode = PackageInfoCompat.getLongVersionCode(archiveInfo)
                if (archiveVersionCode <= currentVersionCode) {
                    updateFile.delete()
                    if (showToastIfUpToDate) {
                        showToast(appContext, "LeFauxPass is up to date.")
                    }
                    return
                }

                if (!hasMatchingSigner(appContext, archiveInfo)) {
                    updateFile.delete()
                    throw SecurityException("Downloaded APK is not signed by the installed app signer.")
                }

                beginInstall(appContext, updateFile)
            } catch (error: Exception) {
                error.printStackTrace()
                if (force || showToastIfUpToDate) {
                    showToast(
                        appContext,
                        "Update failed: ${error.localizedMessage ?: error.javaClass.simpleName}",
                        long = true
                    )
                }
            }
        }
    }

    fun resumePendingInstall(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (!prefs.getBoolean(KEY_WAITING_FOR_INSTALL_PERMISSION, false)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !appContext.packageManager.canRequestPackageInstalls()
        ) {
            return
        }

        prefs.edit().putBoolean(KEY_WAITING_FOR_INSTALL_PERMISSION, false).apply()

        val updateFile = File(appContext.cacheDir, UPDATE_FILE_NAME)
        if (!updateFile.exists()) {
            return
        }

        val archiveInfo = getArchivePackageInfo(appContext, updateFile)
        if (archiveInfo == null ||
            archiveInfo.packageName != appContext.packageName ||
            PackageInfoCompat.getLongVersionCode(archiveInfo) <= installedVersionCode(appContext) ||
            !hasMatchingSigner(appContext, archiveInfo)
        ) {
            updateFile.delete()
            return
        }

        launchPackageInstaller(appContext, updateFile)
    }

    private fun isAutoCheckDue(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK_MS, 0L)
        return System.currentTimeMillis() - lastCheck >= AUTO_CHECK_INTERVAL_MS
    }

    private fun markChecked(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_CHECK_MS, System.currentTimeMillis())
            .apply()
    }

    private fun findUpdateCandidate(currentVersionCode: Long): ReleaseAsset? {
        val releases = JSONArray(httpGetText(RELEASES_API_URL))
        val candidates = mutableListOf<ReleaseAsset>()

        for (releaseIndex in 0 until releases.length()) {
            val release = releases.getJSONObject(releaseIndex)
            if (release.optBoolean("draft", false)) {
                continue
            }

            val assets = release.optJSONArray("assets") ?: continue
            for (assetIndex in 0 until assets.length()) {
                val asset = assets.getJSONObject(assetIndex)
                val name = asset.optString("name")
                val downloadUrl = asset.optString("browser_download_url")

                if (!name.endsWith(".apk", ignoreCase = true) || downloadUrl.isBlank()) {
                    continue
                }

                val buildHint = parseBuildNumber(name)
                if (buildHint != null && buildHint <= currentVersionCode) {
                    continue
                }

                val timestamp = asset.optString("updated_at")
                    .ifBlank { asset.optString("created_at") }
                val createdAt = runCatching { Instant.parse(timestamp) }
                    .getOrDefault(Instant.EPOCH)

                candidates += ReleaseAsset(
                    name = name,
                    downloadUrl = downloadUrl,
                    digest = asset.optString("digest").takeIf { it.isNotBlank() },
                    createdAt = createdAt,
                    buildNumberHint = buildHint
                )
            }
        }

        return candidates.maxWithOrNull(
            compareBy<ReleaseAsset> { it.buildNumberHint ?: Long.MIN_VALUE }
                .thenBy { it.createdAt }
        )
    }

    private fun parseBuildNumber(fileName: String): Long? {
        val match = Regex(
            """.*-(\d+)\.(\d+)\.(\d+)\.(\d+)(?:-[^.]+)?\.apk$""",
            RegexOption.IGNORE_CASE
        ).matchEntire(fileName)

        return match?.groupValues?.getOrNull(4)?.toLongOrNull()
    }

    private fun httpGetText(urlString: String): String {
        val connection = openConnection(urlString)
        return try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val message = connection.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("GitHub returned HTTP $code${message?.let { ": $it" } ?: ""}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadUpdate(context: Context, asset: ReleaseAsset): File {
        val partialFile = File(context.cacheDir, "$UPDATE_FILE_NAME.part")
        val updateFile = File(context.cacheDir, UPDATE_FILE_NAME)

        partialFile.delete()
        updateFile.delete()

        val connection = openConnection(asset.downloadUrl, readTimeoutMs = 60_000)
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("APK download returned HTTP $code")
            }

            connection.inputStream.use { input ->
                FileOutputStream(partialFile).use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }

        verifyDigest(partialFile, asset.digest)

        if (!partialFile.renameTo(updateFile)) {
            partialFile.copyTo(updateFile, overwrite = true)
            partialFile.delete()
        }

        return updateFile
    }

    private fun openConnection(
        urlString: String,
        readTimeoutMs: Int = 15_000
    ): HttpURLConnection {
        return (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = readTimeoutMs
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", API_VERSION)
            setRequestProperty("User-Agent", USER_AGENT)
        }
    }

    private fun verifyDigest(file: File, digest: String?) {
        if (digest.isNullOrBlank()) {
            return
        }

        val parts = digest.split(":", limit = 2)
        if (parts.size != 2 || !parts[0].equals("sha256", ignoreCase = true)) {
            return
        }

        val expected = parts[1].lowercase(Locale.US)
        val actual = sha256(file)

        if (actual != expected) {
            file.delete()
            throw SecurityException("Downloaded APK checksum does not match GitHub.")
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count <= 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun sha256(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
    }

    private fun installedVersionCode(context: Context): Long {
        return PackageInfoCompat.getLongVersionCode(getInstalledPackageInfo(context))
    }

    private fun hasMatchingSigner(context: Context, archiveInfo: PackageInfo): Boolean {
        val installed = signerDigests(getInstalledPackageInfo(context))
        val archive = signerDigests(archiveInfo)
        return installed.isNotEmpty() && installed == archive
    }

    @Suppress("DEPRECATION")
    private fun signerDigests(packageInfo: PackageInfo): Set<String> {
        val signatures: Array<Signature> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
            } else {
                packageInfo.signatures ?: emptyArray()
            }

        return signatures.map { sha256(it.toByteArray()) }.toSet()
    }

    @Suppress("DEPRECATION")
    private fun getInstalledPackageInfo(context: Context): PackageInfo {
        val flags = packageInfoFlags()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, flags)
        }
    }

    @Suppress("DEPRECATION")
    private fun getArchivePackageInfo(context: Context, file: File): PackageInfo? {
        val flags = packageInfoFlags()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            context.packageManager.getPackageArchiveInfo(file.absolutePath, flags)
        }
    }

    private fun packageInfoFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }
    }

    private suspend fun beginInstall(context: Context, updateFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_WAITING_FOR_INSTALL_PERMISSION, true)
                .apply()

            withContext(Dispatchers.Main) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Toast.makeText(
                    context,
                    "Allow installs from LeFauxPass to finish the update.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WAITING_FOR_INSTALL_PERMISSION, false)
            .apply()

        withContext(Dispatchers.Main) {
            launchPackageInstaller(context, updateFile)
        }
    }

    private fun launchPackageInstaller(context: Context, updateFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            updateFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }

    private suspend fun showToast(
        context: Context,
        message: String,
        long: Boolean = false
    ) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                message,
                if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }
}

package com.hereliesaz.lefauxpass

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdater {
    private const val GITHUB_API_URL = "https://api.github.com/repos/hereliesaz/LeFauxPass/releases/latest"

    suspend fun checkForUpdates(context: Context, showToastIfUpToDate: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github+json")
                    connectTimeout = 10000
                    readTimeout = 10000
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseString)
                    val tagName = json.optString("tag_name", "")
                    val assets = json.optJSONArray("assets")

                    var downloadUrl: String? = null
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                downloadUrl = asset.optString("browser_download_url", "")
                                if (downloadUrl.isEmpty()) downloadUrl = null
                                break
                            }
                        }
                    }

                    if (downloadUrl != null) {
                        downloadAndInstall(context, downloadUrl, tagName)
                    } else {
                        if (showToastIfUpToDate) {
                            showToast(context, "No APK asset found in latest release.")
                        }
                    }
                } else {
                    if (showToastIfUpToDate) {
                        showToast(context, "Failed to check for updates: HTTP ${connection.responseCode}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (showToastIfUpToDate) {
                    showToast(context, "Update check failed: ${e.localizedMessage}")
                }
            }
        }
    }

    private suspend fun downloadAndInstall(context: Context, downloadUrl: String, versionName: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Downloading update ($versionName)...", Toast.LENGTH_LONG).show()
        }

        withContext(Dispatchers.IO) {
            try {
                val url = URL(downloadUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val apkFile = File(context.cacheDir, "update.apk")
                    if (apkFile.exists()) apkFile.delete()

                    connection.inputStream.use { input ->
                        FileOutputStream(apkFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        installApk(context, apkFile)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Installation failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

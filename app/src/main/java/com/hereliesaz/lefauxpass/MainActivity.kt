package com.hereliesaz.lefauxpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val hooks = AndroidPlatformHooks(this)
        setContent { App(hooks) }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { GitHubUpdater.resumePendingInstall(this@MainActivity) }
    }
}

/** Android side of the shared UI: SharedPreferences and GitHub updater. */
private class AndroidPlatformHooks(private val activity: AppCompatActivity) : PlatformHooks {
    override fun loadExpiration(): Long? =
        ExpirationManager.getExpirationTime(activity)?.toInstant()?.toEpochMilli()

    override fun saveExpiration(epochMs: Long) = ExpirationManager.setExpirationTime(
        activity,
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())
    )

    private val ticketPreferences = activity.getSharedPreferences("ticket_appearance", Context.MODE_PRIVATE)

    override fun loadRingColor(): String? = ticketPreferences.getString("ring_color", null)

    override fun saveRingColor(argb: String) {
        ticketPreferences.edit().putString("ring_color", argb).apply()
    }

    override fun hasSeenRingColorTip(): Boolean = ticketPreferences.getBoolean("ring_color_tip_seen", false)

    override fun markRingColorTipSeen() {
        ticketPreferences.edit().putBoolean("ring_color_tip_seen", true).apply()
    }

    override fun exit() = activity.finish()

    override val checkForUpdates: (Boolean) -> Unit = { manual ->
        activity.lifecycleScope.launch {
            GitHubUpdater.checkForUpdates(
                context = activity,
                showToastIfUpToDate = manual,
                force = manual
            )
        }
    }

    @Composable
    override fun SystemBarColor(color: Color) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = activity.window
                window.statusBarColor = color.toArgb()
                window.navigationBarColor = color.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    @Composable
    override fun BackHandler(onBack: () -> Unit) = androidx.activity.compose.BackHandler(onBack = onBack)
}

package com.hereliesaz.lefauxpass

import android.annotation.SuppressLint
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
        GitHubUpdater.resumePendingInstall(this)
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

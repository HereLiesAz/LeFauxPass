package com.hereliesaz.lefauxpass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.events.Event

private const val KEY_EXPIRATION = "expiration_time"
private const val KEY_RING_COLOR = "ring_color"
private const val KEY_RING_COLOR_TIP = "ring_color_tip_seen"

/** PWA side of the shared UI: localStorage and browser history. */
object WebPlatformHooks : PlatformHooks {
    override fun loadExpiration(): Long? = localStorage.getItem(KEY_EXPIRATION)?.toLongOrNull()

    override fun saveExpiration(epochMs: Long) = localStorage.setItem(KEY_EXPIRATION, epochMs.toString())

    // Retain appearance in the current page when the browser blocks storage.
    private var sessionRingColor: String? = null
    private var sessionRingColorTipSeen = false

    override fun loadRingColor(): String? =
        sessionRingColor ?: runCatching { localStorage.getItem(KEY_RING_COLOR) }.getOrNull()

    override fun saveRingColor(argb: String) {
        sessionRingColor = argb
        runCatching { localStorage.setItem(KEY_RING_COLOR, argb) }
    }

    override fun hasSeenRingColorTip(): Boolean =
        sessionRingColorTipSeen || runCatching { localStorage.getItem(KEY_RING_COLOR_TIP) == "true" }
            .getOrDefault(false)

    override fun markRingColorTipSeen() {
        sessionRingColorTipSeen = true
        runCatching { localStorage.setItem(KEY_RING_COLOR_TIP, "true") }
    }

    // A page cannot close itself; the browser owns leaving.
    override fun exit() = Unit

    // Updates arrive with each GitHub Pages deploy via the service worker.
    override val checkForUpdates: ((Boolean) -> Unit)? = null

    @Composable
    override fun SystemBarColor(color: Color) {
        SideEffect {
            val hex = "#" + (color.toArgb() and 0xFFFFFF).toString(16).padStart(6, '0')
            (document.querySelector("meta[name=theme-color]") as? HTMLMetaElement)?.content = hex
        }
    }

    /** Pushes a history entry so the browser's back gesture maps to [onBack]. */
    @Composable
    override fun BackHandler(onBack: () -> Unit) {
        val currentOnBack = rememberUpdatedState(onBack)
        DisposableEffect(Unit) {
            var popped = false
            val listener: (Event) -> Unit = {
                popped = true
                currentOnBack.value()
            }
            window.history.pushState(null, "")
            window.addEventListener("popstate", listener)
            onDispose {
                window.removeEventListener("popstate", listener)
                // Left via in-app navigation: drop the entry we pushed.
                if (!popped) window.history.back()
            }
        }
    }
}

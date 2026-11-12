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

/** PWA side of the shared UI: localStorage and browser history. */
object WebPlatformHooks : PlatformHooks {
    override fun loadExpiration(): Long? = localStorage.getItem(KEY_EXPIRATION)?.toLongOrNull()

    override fun saveExpiration(epochMs: Long) = localStorage.setItem(KEY_EXPIRATION, epochMs.toString())

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

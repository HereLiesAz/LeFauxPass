package com.hereliesaz.lefauxpass

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLMetaElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event

private const val KEY_EXPIRATION = "expiration_time"

/** PWA side of the shared UI: localStorage, an HTML <video> overlay, browser history. */
object WebPlatformHooks : PlatformHooks {
    override fun loadExpiration(): Long? = localStorage.getItem(KEY_EXPIRATION)?.toLongOrNull()

    override fun saveExpiration(epochMs: Long) = localStorage.setItem(KEY_EXPIRATION, epochMs.toString())

    // A page cannot close itself; the browser owns leaving.
    override fun exit() = Unit

    // Updates arrive with each GitHub Pages deploy via the service worker.
    override val checkForUpdates: ((Boolean) -> Unit)? = null

    /**
     * Compose renders to a canvas, so the MP4 plays in a DOM <video> laid
     * over the composable's bounds. Same 2% horizontal overscan as Android
     * to crop the source's edge seam.
     */
    @Composable
    override fun AnimationPlayer(modifier: Modifier, onReady: () -> Unit) {
        val density = LocalDensity.current.density
        val currentOnReady = rememberUpdatedState(onReady)
        val frame = remember {
            (document.createElement("div") as HTMLDivElement).apply {
                style.cssText = "position:fixed;overflow:hidden;background:#fff;pointer-events:none;"
                val video = document.createElement("video") as HTMLVideoElement
                video.src = "animation.mp4"
                video.muted = true
                video.loop = true
                video.autoplay = true
                video.setAttribute("playsinline", "")
                video.style.cssText = "width:100%;height:100%;object-fit:contain;transform:scaleX(1.02);"
                appendChild(video)
            }
        }
        DisposableEffect(frame) {
            val video = frame.firstElementChild as HTMLVideoElement
            val ready: (Event) -> Unit = { currentOnReady.value() }
            video.addEventListener("playing", ready)
            document.body?.appendChild(frame)
            video.play()
            onDispose {
                video.removeEventListener("playing", ready)
                video.pause()
                frame.remove()
            }
        }
        Box(modifier.onGloballyPositioned { coords ->
            val r = coords.boundsInWindow()
            frame.style.left = "${r.left / density}px"
            frame.style.top = "${r.top / density}px"
            frame.style.width = "${r.width / density}px"
            frame.style.height = "${r.height / density}px"
        })
    }

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

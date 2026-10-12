package com.hereliesaz.lefauxpass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Everything the shared UI needs from its host. Android implements it in
 * `:app` (ExoPlayer, SharedPreferences, GitHub updater); the PWA implements
 * it in `wasmJsMain` (HTML video overlay, localStorage, browser history).
 */
interface PlatformHooks {
    /** Stored ticket expiration as epoch milliseconds, or null if none. */
    fun loadExpiration(): Long?
    fun saveExpiration(epochMs: Long)

    /** Leaves the app (Ticket Details back arrow). */
    fun exit()

    /** Update check; `manual` bypasses throttling and reports "up to date". Null where unsupported. */
    val checkForUpdates: ((manual: Boolean) -> Unit)?

    /** Looping ticket animation (`app/src/main/res/raw/animation.mp4`). */
    @Composable
    fun AnimationPlayer(modifier: Modifier, onReady: () -> Unit)

    /** Tints the status/navigation bars (Android) or browser theme color (web). */
    @Composable
    fun SystemBarColor(color: Color)

    /** System back (Android) or browser back (web). */
    @Composable
    fun BackHandler(onBack: () -> Unit)
}

val LocalPlatform = staticCompositionLocalOf<PlatformHooks> { error("PlatformHooks not provided") }

/** Wall-clock epoch milliseconds. */
expect fun nowMillis(): Long

/** Local-time calendar fields for an epoch-millisecond instant. */
internal expect fun localParts(epochMs: Long): DateParts

internal data class DateParts(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
)

/** Lifetime of a freshly activated ticket: 1h56m. */
internal const val TICKET_LIFETIME_MS = (60 + 56) * 60 * 1000L

/** US-style formatting, matching the original `java.time` patterns. */
internal object TimeFormat {
    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    private fun Int.pad() = toString().padStart(2, '0')
    private val DateParts.h12 get() = if (hour % 12 == 0) 12 else hour % 12
    private val DateParts.amPm get() = if (hour < 12) "AM" else "PM"

    /** `h:mm:ss a` */
    fun clock(ms: Long) = localParts(ms).run { "$h12:${minute.pad()}:${second.pad()} $amPm" }

    /** `MMM dd, yyyy, h:mm a` */
    fun long(ms: Long) = localParts(ms).run { "${months[month - 1]} ${day.pad()}, $year, $h12:${minute.pad()} $amPm" }

    /** `M/d/yyyy, h:mm a` */
    fun short(ms: Long) = localParts(ms).run { "$month/$day/$year, $h12:${minute.pad()} $amPm" }
}

package com.hereliesaz.lefauxpass

private fun jsNow(): Double = js("Date.now()")
private fun jsYear(ms: Double): Int = js("new Date(ms).getFullYear()")
private fun jsMonth(ms: Double): Int = js("new Date(ms).getMonth() + 1")
private fun jsDay(ms: Double): Int = js("new Date(ms).getDate()")
private fun jsHour(ms: Double): Int = js("new Date(ms).getHours()")
private fun jsMinute(ms: Double): Int = js("new Date(ms).getMinutes()")
private fun jsSecond(ms: Double): Int = js("new Date(ms).getSeconds()")

actual fun nowMillis(): Long = jsNow().toLong()

internal actual fun localParts(epochMs: Long): DateParts {
    val ms = epochMs.toDouble()
    return DateParts(jsYear(ms), jsMonth(ms), jsDay(ms), jsHour(ms), jsMinute(ms), jsSecond(ms))
}

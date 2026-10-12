package com.hereliesaz.lefauxpass

import java.time.Instant
import java.time.ZoneId

actual fun nowMillis(): Long = System.currentTimeMillis()

internal actual fun localParts(epochMs: Long): DateParts =
    Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).run {
        DateParts(year, monthValue, dayOfMonth, hour, minute, second)
    }

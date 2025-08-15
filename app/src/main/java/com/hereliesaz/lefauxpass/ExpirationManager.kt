package com.hereliesaz.lafauxpass

import android.content.Context
import android.content.SharedPreferences
import java.time.ZonedDateTime

object ExpirationManager {

    private const val PREFS_NAME = "RtaTicketPrefs"
    private const val KEY_EXPIRATION_TIME = "expiration_time"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getExpirationTime(context: Context): ZonedDateTime? {
        val prefs = getSharedPreferences(context)
        val expirationTimestamp = prefs.getLong(KEY_EXPIRATION_TIME, 0L)
        return if (expirationTimestamp == 0L) {
            null
        } else {
            ZonedDateTime.ofInstant(java.time.Instant.ofEpochSecond(expirationTimestamp), java.time.ZoneId.systemDefault())
        }
    }

    fun setExpirationTime(context: Context, expirationTime: ZonedDateTime) {
        val prefs = getSharedPreferences(context)
        val editor = prefs.edit()
        val expirationTimestamp = expirationTime.toEpochSecond()
        editor.putLong(KEY_EXPIRATION_TIME, expirationTimestamp)
        editor.apply()
    }
}

package com.example.security

import android.content.Context
import android.content.SharedPreferences

/** Persistent security settings (survive process death) backed by SharedPreferences. */
class SecurityPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("onyx_security", Context.MODE_PRIVATE)

    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    var autoLockOption: String
        get() = prefs.getString(KEY_AUTO_LOCK, AUTO_LOCK_IMMEDIATE) ?: AUTO_LOCK_IMMEDIATE
        set(value) = prefs.edit().putString(KEY_AUTO_LOCK, value).apply()

    var hideCodesWhenLocked: Boolean
        get() = prefs.getBoolean(KEY_HIDE_CODES, false)
        set(value) = prefs.edit().putBoolean(KEY_HIDE_CODES, value).apply()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    companion object {
        const val AUTO_LOCK_IMMEDIATE = "فوراً"
        const val AUTO_LOCK_ONE_MINUTE = "بعد دقيقة واحدة"

        /** Grace period in milliseconds before the app re-locks after going to background. */
        fun autoLockMillis(option: String): Long = when (option) {
            AUTO_LOCK_ONE_MINUTE -> 60_000L
            else -> 0L
        }

        private const val KEY_BIOMETRIC = "biometric_enabled"
        private const val KEY_AUTO_LOCK = "auto_lock_option"
        private const val KEY_HIDE_CODES = "hide_codes_when_locked"
        private const val KEY_FIRST_LAUNCH = "is_first_launch"
    }
}

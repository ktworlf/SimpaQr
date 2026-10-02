package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auraqr_prefs", Context.MODE_PRIVATE)

    companion object {
        const val MAX_FREE_DAILY_SCANS = 5
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DAILY_SCANS = "key_daily_scans"
        private const val KEY_LAST_DATE = "key_last_date"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_AUTO_COPY = "key_auto_copy"
        private const val KEY_SAFE_BROWSING = "key_safe_browsing"
        private const val KEY_SOUND = "key_sound"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_IS_PRO = "key_is_pro"
    }

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dailyScanCount = MutableStateFlow(loadDailyScanCount())
    val dailyScanCount: StateFlow<Int> = _dailyScanCount.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isAutoCopyEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_COPY, false))
    val isAutoCopyEnabled: StateFlow<Boolean> = _isAutoCopyEnabled.asStateFlow()

    private val _isSafeBrowsingEnabled = MutableStateFlow(prefs.getBoolean(KEY_SAFE_BROWSING, true))
    val isSafeBrowsingEnabled: StateFlow<Boolean> = _isSafeBrowsingEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString(KEY_USER_EMAIL, null))
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Aura User") ?: "Aura User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(prefs.getBoolean(KEY_IS_PRO, false))
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun loadDailyScanCount(): Int {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "") ?: ""
        if (today != lastDate) {
            // New day, reset count
            prefs.edit()
                .putString(KEY_LAST_DATE, today)
                .putInt(KEY_DAILY_SCANS, 0)
                .apply()
            return 0
        }
        return prefs.getInt(KEY_DAILY_SCANS, 0)
    }

    fun canUserScan(): Boolean {
        if (_isProUnlocked.value || !_userEmail.value.isNullOrBlank()) {
            return true
        }
        return loadDailyScanCount() < MAX_FREE_DAILY_SCANS
    }

    fun remainingScans(): Int {
        if (_isProUnlocked.value || !_userEmail.value.isNullOrBlank()) {
            return Int.MAX_VALUE
        }
        val count = loadDailyScanCount()
        return (MAX_FREE_DAILY_SCANS - count).coerceAtLeast(0)
    }

    fun incrementDailyScan(): Boolean {
        if (_isProUnlocked.value || !_userEmail.value.isNullOrBlank()) {
            val count = loadDailyScanCount() + 1
            prefs.edit().putInt(KEY_DAILY_SCANS, count).apply()
            _dailyScanCount.value = count
            return true
        }

        val count = loadDailyScanCount()
        if (count >= MAX_FREE_DAILY_SCANS) {
            return false // Scan limit reached!
        }
        val next = count + 1
        val today = getTodayDateString()
        prefs.edit()
            .putString(KEY_LAST_DATE, today)
            .putInt(KEY_DAILY_SCANS, next)
            .apply()
        _dailyScanCount.value = next
        return true
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _isHapticEnabled.value = enabled
    }

    fun setAutoCopyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_COPY, enabled).apply()
        _isAutoCopyEnabled.value = enabled
    }

    fun setSafeBrowsingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAFE_BROWSING, enabled).apply()
        _isSafeBrowsingEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _isSoundEnabled.value = enabled
    }

    fun setUserProfile(email: String?, name: String?, isPro: Boolean = true) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name ?: "Aura User")
            .putBoolean(KEY_IS_PRO, isPro)
            .apply()
        _userEmail.value = email
        _userName.value = name ?: "Aura User"
        _isProUnlocked.value = isPro
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_USER_EMAIL)
            .putString(KEY_USER_NAME, "Guest User")
            .putBoolean(KEY_IS_PRO, false)
            .apply()
        _userEmail.value = null
        _userName.value = "Guest User"
        _isProUnlocked.value = false
    }
}

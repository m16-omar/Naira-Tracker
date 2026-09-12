package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("naira_tracker_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getSavedThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private fun getSavedThemeMode(): ThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(saved)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
    }
}

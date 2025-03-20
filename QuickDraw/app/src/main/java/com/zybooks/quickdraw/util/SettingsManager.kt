package com.zybooks.quickdraw.util

import android.content.Context
import android.content.SharedPreferences
import com.zybooks.quickdraw.model.GameSettings

// saving and loading of game settings
class SettingsManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    // loads settings from SharedPreferences or returns defaults
    fun loadSettings(): GameSettings {
        return GameSettings(
            timerDurationSeconds = preferences.getInt(KEY_TIMER_DURATION, DEFAULT_TIMER_DURATION),
            minPlayers = preferences.getInt(KEY_MIN_PLAYERS, DEFAULT_MIN_PLAYERS),
            maxPlayers = preferences.getInt(KEY_MAX_PLAYERS, DEFAULT_MAX_PLAYERS),
            enableSoundEffects = preferences.getBoolean(KEY_SOUND_EFFECTS, DEFAULT_SOUND_EFFECTS),
            enableVibration = preferences.getBoolean(KEY_VIBRATION, DEFAULT_VIBRATION)
        )
    }

    // saves game settings to SharedPreferences
    fun saveSettings(settings: GameSettings) {
        preferences.edit().apply {
            putInt(KEY_TIMER_DURATION, settings.timerDurationSeconds)
            putInt(KEY_MIN_PLAYERS, settings.minPlayers)
            putInt(KEY_MAX_PLAYERS, settings.maxPlayers)
            putBoolean(KEY_SOUND_EFFECTS, settings.enableSoundEffects)
            putBoolean(KEY_VIBRATION, settings.enableVibration)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "quickdraw_settings"

        private const val KEY_TIMER_DURATION = "timer_duration"
        private const val KEY_MIN_PLAYERS = "min_players"
        private const val KEY_MAX_PLAYERS = "max_players"
        private const val KEY_SOUND_EFFECTS = "sound_effects"
        private const val KEY_VIBRATION = "vibration"

        private const val DEFAULT_TIMER_DURATION = 15
        private const val DEFAULT_MIN_PLAYERS = 2
        private const val DEFAULT_MAX_PLAYERS = 8
        private const val DEFAULT_SOUND_EFFECTS = true
        private const val DEFAULT_VIBRATION = true
    }
}
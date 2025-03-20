package com.zybooks.quickdraw.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zybooks.quickdraw.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // shared preferences constants
    private val PREFS_NAME = "quickdraw_settings"
    private val KEY_TIMER_DURATION = "timer_duration"
    private val KEY_MIN_PLAYERS = "min_players"
    private val KEY_MAX_PLAYERS = "max_players"
    private val KEY_SOUND_EFFECTS = "sound_effects"
    private val KEY_VIBRATION = "vibration"

    // get SharedPreferences directly for settings changes
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // load saved settings directly from SharedPreferences
        val timerDuration = preferences.getInt(KEY_TIMER_DURATION, 15)
        val minPlayers = preferences.getInt(KEY_MIN_PLAYERS, 2)
        val maxPlayers = preferences.getInt(KEY_MAX_PLAYERS, 8)
        val soundEffects = preferences.getBoolean(KEY_SOUND_EFFECTS, true)
        val vibration = preferences.getBoolean(KEY_VIBRATION, true)

        val savedSettings = GameSettings(
            timerDurationSeconds = timerDuration,
            minPlayers = minPlayers,
            maxPlayers = maxPlayers,
            enableSoundEffects = soundEffects,
            enableVibration = vibration
        )

        _uiState.value = _uiState.value.copy(settings = savedSettings)
    }

    fun updateTimerDuration(seconds: Int) {
        if (seconds in 5..60) {
            val updatedSettings = _uiState.value.settings.copy(timerDurationSeconds = seconds)
            _uiState.value = _uiState.value.copy(settings = updatedSettings)
        }
    }

    fun updateMinPlayers(count: Int) {
        if (count in 2.._uiState.value.settings.maxPlayers) {
            val updatedSettings = _uiState.value.settings.copy(minPlayers = count)
            _uiState.value = _uiState.value.copy(settings = updatedSettings)
        }
    }

    fun updateMaxPlayers(count: Int) {
        if (count >= _uiState.value.settings.minPlayers && count <= 8) {
            val updatedSettings = _uiState.value.settings.copy(maxPlayers = count)
            _uiState.value = _uiState.value.copy(settings = updatedSettings)
        }
    }

    fun toggleSoundEffects() {
        val updatedSettings = _uiState.value.settings.copy(
            enableSoundEffects = !_uiState.value.settings.enableSoundEffects
        )
        _uiState.value = _uiState.value.copy(settings = updatedSettings)
    }

    fun toggleVibration() {
        val updatedSettings = _uiState.value.settings.copy(
            enableVibration = !_uiState.value.settings.enableVibration
        )
        _uiState.value = _uiState.value.copy(settings = updatedSettings)
    }

    fun saveSettings() {
        // save to SharedPreferences
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            // curr settings
            val settings = _uiState.value.settings

            // save to SharedPreferences
            preferences.edit().apply {
                putInt(KEY_TIMER_DURATION, settings.timerDurationSeconds)
                putInt(KEY_MIN_PLAYERS, settings.minPlayers)
                putInt(KEY_MAX_PLAYERS, settings.maxPlayers)
                putBoolean(KEY_SOUND_EFFECTS, settings.enableSoundEffects)
                putBoolean(KEY_VIBRATION, settings.enableVibration)
                apply() // apply changes asynchronously?
            }

            // show saving feedback so user sees the success or failure!
            kotlinx.coroutines.delay(300)

            _uiState.value = _uiState.value.copy(
                isSaving = false,
                settingsSaved = true,
                navigateBack = true
            )
        }
    }

    fun resetSettings() {
        _uiState.value = _uiState.value.copy(
            settings = GameSettings()  // reset to defaults
        )
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateBack = false)
    }
}

data class SettingsUiState(
    val settings: GameSettings = GameSettings(),
    val isSaving: Boolean = false,
    val settingsSaved: Boolean = false,
    val navigateBack: Boolean = false
)
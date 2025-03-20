package com.zybooks.quickdraw.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onStartGameClicked() {
        _uiState.value = _uiState.value.copy(navigateToGameSetup = true)
    }

    fun onLeaderboardClicked() {
        _uiState.value = _uiState.value.copy(navigateToLeaderboard = true)
    }

    fun onSettingsClicked() {
        _uiState.value = _uiState.value.copy(navigateToSettings = true)
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(
            navigateToGameSetup = false,
            navigateToLeaderboard = false,
            navigateToSettings = false
        )
    }
}

data class HomeUiState(
    val navigateToGameSetup: Boolean = false,
    val navigateToLeaderboard: Boolean = false,
    val navigateToSettings: Boolean = false
)

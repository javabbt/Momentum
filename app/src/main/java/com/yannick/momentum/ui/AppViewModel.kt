package com.yannick.momentum.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yannick.core.theme.ThemePreferences
import com.yannick.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val themePreferences: ThemePreferences,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.isDarkMode.collectLatest { isDark ->
                _uiState.update {
                    it.copy(isDarkMode = isDark, isLoggedIn = authRepository.isLoggedIn())
                }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.saveDarkMode(!_uiState.value.isDarkMode)
        }
    }
}

data class UiState(
    val isDarkMode: Boolean = false,
    val isLoggedIn: Boolean = true,
)

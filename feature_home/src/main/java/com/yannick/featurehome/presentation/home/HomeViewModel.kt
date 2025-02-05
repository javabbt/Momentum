package com.yannick.featurehome.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.yannick.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onTabSelected(index: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedIndex = index) }
        }
    }
}

data class UiState(
    val selectedIndex: Int = 0,
    val user: FirebaseUser? = null,
)

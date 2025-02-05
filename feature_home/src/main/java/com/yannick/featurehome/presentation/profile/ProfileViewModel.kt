package com.yannick.featurehome.presentation.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val firestoreService: FirestoreService,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun logout() {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            authRepository.logout()
            sendSideEffect(SideEffect.LogoutDone)
        }
    }

    init {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            authRepository.getCurrentUser()?.let { user ->
                val shortUserName = if (user.uid.length > 5) user.uid.slice(0..5) else user.uid
                _uiState.update {
                    it.copy(
                        displayName = user.displayName ?: "",
                        profilePicture = user.photoUrl?.toString(),
                        username = user.uid,
                        shortUserName = shortUserName
                    )
                }
            }
            when (val stats = firestoreService.getProfileOfUser(_uiState.value.username)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            chains = stats.output.chains,
                            friends = stats.output.friends,
                            streaks = stats.output.streaks
                        )
                    }
                }

                is Result.Error -> {
                    sendSideEffect(SideEffect.ShowError(stats.exception.message ?: "Error"))
                }

                is Result.UnexpectedError -> {
                    sendSideEffect(SideEffect.ShowUnexpectedError(com.yannick.resources.R.string.unexpected_error))
                }
            }
        }
    }

    private fun sendSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            _sideEffects.emit(sideEffect)
        }
    }
}

sealed class SideEffect {
    class ShowError(val msg: String) : SideEffect()
    class ShowUnexpectedError(@StringRes val msg: Int) : SideEffect()
    data object LogoutDone : SideEffect()
}

data class ProfileUiState(
    val chains: Int = 0,
    val friends: Int = 0,
    val streaks: Int = 0,
    val username: String = "",
    val profilePicture: String? = null,
    val displayName: String = "",
    val shortUserName: String = ""
)
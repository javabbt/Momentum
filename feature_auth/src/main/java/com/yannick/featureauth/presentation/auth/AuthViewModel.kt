package com.yannick.featureauth.presentation.auth

import androidx.annotation.StringRes
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.domain.usecases.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    private fun sendSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            _sideEffects.emit(sideEffect)
        }
    }

    fun signInWithGoogle(credential: Credential) = viewModelScope.launch {
        try {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential =
                    GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                signInWithGoogleUseCase(firebaseCredential)?.let { firebaseUser ->
                    _uiState.update { it.copy(user = firebaseUser) }
                }
                sendSideEffect(SideEffect.LoginDone)
            } else {
                Timber.e("UNEXPECTED_CREDENTIAL")
                sendSideEffect(SideEffect.ShowUnexpectedError(com.yannick.resources.R.string.unexpected_credential))
            }
        } catch (e: Exception) {
            Timber.e(e)
            e.message?.let { sendSideEffect(SideEffect.ShowError(it)) }
        }
    }
}

data class UiState(
    val user: FirebaseUser? = null,
)

sealed class SideEffect {
    class ShowError(val msg: String) : SideEffect()
    class ShowUnexpectedError(@StringRes val msg: Int) : SideEffect()
    data object LoginDone : SideEffect()
}

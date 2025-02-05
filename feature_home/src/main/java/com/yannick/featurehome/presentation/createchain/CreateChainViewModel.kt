package com.yannick.featurehome.presentation.createchain

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Chain
import com.yannick.domain.models.ChainPhoto
import com.yannick.domain.models.ChainStatus
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import java.util.UUID

class CreateChainViewModel(
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val fireStoreService: FirestoreService,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateChainUiModel())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffects.asSharedFlow()

    fun onImageSelected(image: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(photo = image)
            }
        }
    }

    private fun sendSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            _sideEffects.emit(sideEffect)
        }
    }

    suspend fun onSubmit(theme: String, caption: String, uri: Uri?) {
        if (theme.isEmpty() || caption.isEmpty() || uri == null) {
            sendSideEffect(SideEffect.ShowUnexpectedError(com.yannick.resources.R.string.chain_error))
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(loading = true)
            }
        }

        val participantsData =
            fireStoreService.getFriendsOfUser(authRepository.getCurrentUser()!!.uid)
        val participants = if (participantsData is Result.Success) {
            participantsData.output.map { it.uid }
        } else {
            emptyList()
        }
        val newPart = participants.plus(authRepository.getCurrentUser()!!.uid)
        val chainId = UUID.randomUUID().toString()
        val id = UUID.randomUUID().toString()
        val chain = Chain(
            id = chainId,
            theme = theme,
            createdBy = authRepository.getCurrentUser()!!.uid,
            participants = newPart,
            deadline = LocalDateTime.now().plusDays(1).toString(),
            streak = 0,
            photos = listOf(
                ChainPhoto(
                    id = id,
                    chainId = chainId,
                    userId = authRepository.getCurrentUser()!!.uid,
                    photoUrl = uri.toString(),
                    timestamp = LocalDateTime.now().toString(),
                ),
            ),
            chainStatus = ChainStatus.ACTIVE.toString(),
            caption = caption,
        )
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            try {
                fireStoreService.addChain(theme, chain)
                sendSideEffect(SideEffect.UploadDone)
                _uiState.update {
                    it.copy(loading = false)
                }
            } catch (e: Exception) {
                sendSideEffect(SideEffect.ShowError(e))
            }
        }
    }
}

sealed class SideEffect {
    class ShowError(val msg: Exception) : SideEffect()
    class ShowUnexpectedError(@StringRes val msg: Int) : SideEffect()
    data object UploadDone : SideEffect()
}

data class CreateChainUiModel(
    val theme: String = "",
    val caption: String = "",
    val photo: String? = null,
    val loading: Boolean = false,
)

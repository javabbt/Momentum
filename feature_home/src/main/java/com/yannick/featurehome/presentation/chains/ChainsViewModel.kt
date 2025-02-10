package com.yannick.featurehome.presentation.chains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Chain
import com.yannick.featurehome.domain.repositories.ChainsRepository
import com.yannick.featurehome.presentation.shared.PhotosHolder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChainsViewModel(
    private val chainsRepository: ChainsRepository,
    private val firestoreService: FirestoreService,
    private val coroutinesDispatcherProvider: CoroutinesDispatcherProvider,
    private val photosHolder: PhotosHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChainState())
    val state = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SideEffect>()
    val sideEffect = _sideEffect.asSharedFlow()

    private val _chains = MutableStateFlow<PagingData<Chain>>(PagingData.empty())
    val chains: StateFlow<PagingData<Chain>> = _chains.asStateFlow()

    init {
        refreshChains()
    }

    private fun sendSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch {
            _sideEffect.emit(sideEffect)
        }
    }

    fun setPhotos(photos: List<String>) {
        photosHolder.photos.value = photos
    }

    fun onImageSelected(image: String, chain: Chain) {
        viewModelScope.launch(coroutinesDispatcherProvider.io) {
            _uiState.update { it.copy(isLoading = true) }
            firestoreService.uploadImageAndUpdateChain(image, chain)
            _uiState.update { it.copy(isLoading = false) }
            sendSideEffect(SideEffect.UploadDone)
        }
    }

    private fun refreshChains() {
        viewModelScope.launch {
            chainsRepository.getChains()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _chains.update { pagingData }
                }
        }
    }

    fun reloadChains() {
        refreshChains()
    }
}

data class ChainState(
    val isLoading: Boolean = false,
)

sealed class SideEffect {
    object UploadDone : SideEffect()
}

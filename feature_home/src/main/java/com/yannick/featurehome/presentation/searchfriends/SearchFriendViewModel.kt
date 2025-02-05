package com.yannick.featurehome.presentation.searchfriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Friend
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchFriendViewModel(
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<SideEffects>()
    val sideEffects: SharedFlow<SideEffects> = _sideEffects.asSharedFlow()
    fun searchFriends(query: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }
            when (val friends = firestoreService.searchFriends(query)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            friends = friends.output.map { friend ->
                                FriendFollow(
                                    friend = friend,
                                    isFollowing = firestoreService.canFollowUser(
                                        userId = authRepository.getCurrentUser()!!.uid,
                                        friendId = friend.uid
                                    )
                                )
                            },
                            isLoading = false
                        )
                    }
                }

                else -> {
                    _sideEffects.emit(SideEffects.NothingFound)
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun followFriend(friend: FriendFollow) {
        viewModelScope.launch {
            if (friend.isFollowing) {
                firestoreService.unfollowUser(
                    userId = authRepository.getCurrentUser()!!.uid,
                    friendId = friend.friend
                )
            } else {
                firestoreService.followUser(
                    userId = authRepository.getCurrentUser()!!.uid,
                    friendId = friend.friend
                )
            }
            _uiState.update { state ->
                state.copy(
                    friends = state.friends.map {
                        if (it.friend.uid == friend.friend.uid) {
                            it.copy(isFollowing = !it.isFollowing)
                        } else {
                            it
                        }
                    }
                )
            }
        }
    }
}

data class FriendFollow(
    val friend: Friend,
    val isFollowing: Boolean
)

data class FriendsUiState(
    val friends: List<FriendFollow> = emptyList(),
    val isLoading: Boolean = false,
)

sealed class SideEffects {
    data object NothingFound : SideEffects()
}
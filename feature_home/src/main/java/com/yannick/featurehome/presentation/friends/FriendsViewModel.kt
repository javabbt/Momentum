package com.yannick.featurehome.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Friend
import com.yannick.domain.repositories.AuthRepository
import com.yannick.featurehome.domain.repositories.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val friendsRepository: FriendsRepository,
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository,
) : ViewModel() {

    // Use StateFlow to hold the friends list
    private val _friends = MutableStateFlow<PagingData<Friend>>(PagingData.empty())
    val friends: StateFlow<PagingData<Friend>> = _friends.asStateFlow()

    init {
        refreshFriends()
    }

    // Function to fetch friends and update StateFlow
    private fun refreshFriends() {
        viewModelScope.launch {
            friendsRepository.getProducts()
                .cachedIn(viewModelScope)
                .collect { pagingData ->
                    _friends.value = pagingData
                }
        }
    }

    fun unfollow(friend: Friend) {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { currentUser ->
                firestoreService.unfollowUser(currentUser.uid, friend)
                refreshFriends() // Refresh after unfollowing
            }
        }
    }
}

package com.yannick.featurehome.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.Friend
import com.yannick.domain.repositories.AuthRepository
import com.yannick.featurehome.domain.repositories.FriendsRepository
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val friendsRepository: FriendsRepository,
    private val firestoreService: FirestoreService,
    private val authRepository: AuthRepository
) : ViewModel() {
    val friends = friendsRepository.getProducts()
        .cachedIn(viewModelScope)

    fun unfollow(friend: Friend) {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { currentUser ->
                firestoreService.unfollowUser(currentUser.uid, friend)
            }
        }
    }
}

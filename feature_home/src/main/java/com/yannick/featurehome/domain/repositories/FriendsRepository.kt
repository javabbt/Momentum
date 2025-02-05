package com.yannick.featurehome.domain.repositories

import androidx.paging.PagingData
import com.yannick.domain.models.Friend
import kotlinx.coroutines.flow.Flow

interface FriendsRepository {
    fun getProducts(): Flow<PagingData<Friend>>
}

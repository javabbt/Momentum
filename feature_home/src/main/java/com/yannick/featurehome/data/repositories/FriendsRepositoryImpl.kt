package com.yannick.featurehome.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.yannick.domain.models.Friend
import com.yannick.featurehome.data.paging.FriendsPagingSource
import com.yannick.featurehome.domain.repositories.FriendsRepository
import kotlinx.coroutines.flow.Flow

const val PAGE_SIZE = 10L

class FriendsRepositoryImpl(
    private val source: FriendsPagingSource,
    private val config: PagingConfig,
) : FriendsRepository {
    override fun getProducts(): Flow<PagingData<Friend>> {
        return Pager(
            config = config,
        ) {
            source
        }.flow
    }
}

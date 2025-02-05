package com.yannick.featurehome.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yannick.domain.models.Friend
import kotlinx.coroutines.tasks.await

class FriendsPagingSource(
    private val queryFriends: Query,
) : PagingSource<QuerySnapshot, Friend>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Friend>): QuerySnapshot? = null

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Friend> =
        try {
            val currentPage = params.key ?: queryFriends.get().await()
            val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryFriends.startAfter(lastVisibleProduct).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(Friend::class.java),
                prevKey = null,
                nextKey = nextPage,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}

package com.yannick.featurehome.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yannick.domain.models.Chain
import kotlinx.coroutines.tasks.await

class ChainsPagingSource(
    private val queryChains: Query,
) : PagingSource<QuerySnapshot, Chain>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Chain>): QuerySnapshot? = null

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Chain> =
        try {
            val currentPage = params.key ?: queryChains.get().await()
            val lastVisibleChain = currentPage.documents[currentPage.size() - 1]
            val nextPage = queryChains.startAfter(lastVisibleChain).get().await()
            LoadResult.Page(
                data = currentPage.toObjects(Chain::class.java),
                prevKey = null,
                nextKey = nextPage,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}

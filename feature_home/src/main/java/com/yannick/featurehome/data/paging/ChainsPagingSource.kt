package com.yannick.featurehome.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yannick.data.services.USER_CHAINS
import com.yannick.domain.models.Chain
import com.yannick.featurehome.data.repositories.PAGE_SIZE
import kotlinx.coroutines.tasks.await
import org.joda.time.LocalDateTime

class ChainsPagingSource(
    private val firebaseFireStore: FirebaseFirestore,
) : PagingSource<QuerySnapshot, Chain>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Chain>): QuerySnapshot? = null

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Chain> =
        try {
            val queryChains = firebaseFireStore.collection(USER_CHAINS).let { collection ->
                FirebaseAuth.getInstance().uid?.let { uid ->
                    collection.document(uid).collection(USER_CHAINS)
                        .whereGreaterThan("deadline", LocalDateTime.now().toString())
                        .orderBy("deadline", Query.Direction.DESCENDING)
                        .limit(PAGE_SIZE)
                }
            }
            if (queryChains == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null,
                )
            } else {
                val currentPage = params.key ?: queryChains.get().await()
                val lastVisibleChain = currentPage.documents[currentPage.size() - 1]
                val nextPage = queryChains.startAfter(lastVisibleChain).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Chain::class.java),
                    prevKey = null,
                    nextKey = nextPage,
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}

package com.yannick.featurehome.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yannick.data.services.FOLLOWERS_ROW
import com.yannick.domain.models.Friend
import kotlinx.coroutines.tasks.await

class FriendsPagingSource(
    private val firebaseFirestore: FirebaseFirestore
) : PagingSource<QuerySnapshot, Friend>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, Friend>): QuerySnapshot? = null

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Friend> =
        try {
            val queryFriends = firebaseFirestore.collection(FOLLOWERS_ROW)
                .let { collection ->
                    FirebaseAuth.getInstance().uid?.let { uid ->
                        collection.document(uid).collection(FOLLOWERS_ROW)
                            .orderBy("userName", Query.Direction.ASCENDING)
                    }
                }
            if (queryFriends == null) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                val currentPage = params.key ?: queryFriends.get().await()
                val lastVisibleProduct = currentPage.documents[currentPage.size() - 1]
                val nextPage = queryFriends.startAfter(lastVisibleProduct).get().await()
                LoadResult.Page(
                    data = currentPage.toObjects(Friend::class.java),
                    prevKey = null,
                    nextKey = nextPage,
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}

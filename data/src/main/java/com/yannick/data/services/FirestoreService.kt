package com.yannick.data.services

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yannick.domain.models.Chain
import com.yannick.domain.models.ChainPhoto
import com.yannick.domain.models.Friend
import com.yannick.domain.models.ProfileStats
import com.yannick.domain.utils.Result
import kotlinx.coroutines.tasks.await
import java.util.UUID

const val USERS_COLLECTION = "users"
const val FRIENDS_ROW = "friends"
const val CHAINS_ROW = "chains"
const val STREAKS_ROW = "streaks"
const val USER_NAME_ROW = "userName"
const val PROFILE_PICTURE_ROW = "profilePicture"
const val FOLLOWERS_ROW = "followers"
const val CHAINS = "chains"
const val USER_CHAINS = "user_chains"
const val UID = "uid"

interface FirestoreService {
    suspend fun getProfileOfUser(userId: String): Result<ProfileStats>
    suspend fun updateProfileOfUser(userId: String, profileStats: ProfileStats): Result<Unit>
    suspend fun getFriendsOfUser(userId: String): Result<List<Friend>>
    suspend fun addChain(userId: String, chain: Chain): Result<Unit>
    suspend fun uploadImageAndUpdateChain(photoUrl: String, chain: Chain): Result<Unit>
    suspend fun searchFriends(query: String): Result<List<Friend>>
    suspend fun canFollowUser(userId: String, friendId: String): Boolean
    suspend fun followUser(userId: String, friendId: Friend): Result<Unit>
    suspend fun unfollowUser(userId: String, friendId: Friend): Result<Unit>
}

class FirestoreServiceImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
) : FirestoreService {
    override suspend fun getProfileOfUser(userId: String): Result<ProfileStats> {
        return try {
            val userDocument = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (userDocument.exists()) {
                Result.Success(
                    userDocument.data?.toProfileStats() ?: ProfileStats(
                        0,
                        0,
                        0,
                        "",
                        "",
                        "",
                    ),
                )
            } else {
                Result.Success(ProfileStats(0, 0, 0, "", "", ""))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun canFollowUser(userId: String, friendId: String): Boolean {
        try {
            val follow =
                firestore.collection(FOLLOWERS_ROW).document(userId).collection(FOLLOWERS_ROW)
                    .document(friendId).get().await()
            return follow.exists()
        } catch (e: Exception) {
            return false
        }
    }

    override suspend fun searchFriends(query: String): Result<List<Friend>> {
        return try {
            val friends = mutableListOf<Friend>()
            val userDocument = firestore.collection(USERS_COLLECTION).get().await()
            userDocument.documents.forEach { doc ->
                doc.toObject(Friend::class.java)?.let { friend ->
                    if (friend.userName.contains(query, ignoreCase = true)) {
                        friends.add(friend)
                    }
                }
            }
            return Result.Success(friends)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun uploadImageAndUpdateChain(photoUrl: String, chain: Chain): Result<Unit> {
        try {
            val imagesRef: StorageReference = firebaseStorage.reference.child("images")
            val spaceRef = imagesRef.child(UUID.randomUUID().toString())
            val task = spaceRef.putFile(Uri.parse(photoUrl)).await()
            task.storage.downloadUrl.await().toString().let { url ->
                val chainPhoto = ChainPhoto(
                    id = UUID.randomUUID().toString(),
                    chainId = chain.id,
                    photoUrl = url,
                    timestamp = org.joda.time.LocalDateTime.now().toString(),
                    userId = chain.createdBy,
                )
                val newList = arrayListOf<ChainPhoto>()
                newList.addAll(chain.photos)
                newList.add(chainPhoto)
                firestore.collection(CHAINS)
                    .document(chain.id)
                    .update("photos", newList)
            }
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun addChain(userId: String, chain: Chain): Result<Unit> {
        try {
            val imagesRef: StorageReference = firebaseStorage.reference.child("images")
            val spaceRef = imagesRef.child(UUID.randomUUID().toString())
            val task = spaceRef.putFile(Uri.parse(chain.photos[0].photoUrl)).await()
            task.storage.downloadUrl.await().toString().let { url ->
                val newChain = chain.copy(photos = listOf(chain.photos[0].copy(photoUrl = url)))
                firestore.collection(CHAINS).add(newChain).await()
            }
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun updateProfileOfUser(
        userId: String,
        profileStats: ProfileStats,
    ): Result<Unit> {
        return try {
            val onlineValue = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (onlineValue.exists()) {
                onlineValue.data?.toProfileStats()?.copy(
                    userName = profileStats.userName,
                    profilePicture = profileStats.profilePicture,
                )?.let { data ->
                    firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .update(data.toMap())
                        .await()
                }
            } else {
                firestore.collection(USERS_COLLECTION).document(userId).set(profileStats.toMap())
                    .await()
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getFriendsOfUser(userId: String): Result<List<Friend>> {
        return try {
            val userDocument =
                firestore.collection(FOLLOWERS_ROW).document(userId).collection(FOLLOWERS_ROW).get()
                    .await()
            val friends = mutableListOf<Friend>()
            userDocument.documents.forEach { doc ->
                doc.toObject(Friend::class.java)?.let { friends.add(it) }
            }
            return Result.Success(friends)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun followUser(userId: String, friendId: Friend): Result<Unit> {
        return try {
            firestore.collection(FOLLOWERS_ROW).document(userId).collection(FOLLOWERS_ROW)
                .document(friendId.uid).set(friendId).await()
            firestore.collection(FOLLOWERS_ROW).document(friendId.uid).collection(FOLLOWERS_ROW)
                .document(userId)
                .set(Friend(friendId.uid, friendId.userName, friendId.profilePicture))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun unfollowUser(userId: String, friendId: Friend): Result<Unit> {
        return try {
            firestore.collection(FOLLOWERS_ROW).document(userId).collection(FOLLOWERS_ROW)
                .document(friendId.uid).delete().await()
            firestore.collection(FOLLOWERS_ROW).document(friendId.uid).collection(FOLLOWERS_ROW)
                .document(userId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

fun Map<String, Any>.toProfileStats(): ProfileStats {
    return ProfileStats(
        friends = get(FRIENDS_ROW) as? Int ?: 0,
        chains = get(CHAINS_ROW) as? Int ?: 0,
        streaks = get(STREAKS_ROW) as? Int ?: 0,
        userName = get(USER_NAME_ROW) as? String ?: "",
        profilePicture = get(PROFILE_PICTURE_ROW) as? String ?: "",
        uid = get(UID) as? String ?: "",
    )
}

fun ProfileStats.toMap(): Map<String, Any> {
    return mapOf(
        FRIENDS_ROW to friends,
        CHAINS_ROW to chains,
        STREAKS_ROW to streaks,
        USER_NAME_ROW to userName,
        PROFILE_PICTURE_ROW to profilePicture,
        UID to uid,
    )
}

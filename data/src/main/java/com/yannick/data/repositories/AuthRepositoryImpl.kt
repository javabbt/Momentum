package com.yannick.data.repositories

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.yannick.data.datasource.FirebaseAuthDataSource
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.ProfileStats
import com.yannick.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val firestoreService: FirestoreService,
) : AuthRepository {

    override suspend fun signInWithGoogle(credential: AuthCredential): FirebaseUser? {
        val firebaseCredential = authDataSource.signInWithCredential(credential)
        if (firebaseCredential != null) {
            firestoreService.updateProfileOfUser(
                userId = firebaseCredential.uid,
                profileStats = ProfileStats(
                    0,
                    0,
                    0,
                    firebaseCredential.displayName ?: "",
                    profilePicture = firebaseCredential.photoUrl?.toString() ?: "",
                    uid = firebaseCredential.uid,
                ),
            )
        }
        return firebaseCredential
    }

    override fun getCurrentUser(): FirebaseUser? {
        return authDataSource.getCurrentUser()
    }

    override fun isLoggedIn(): Boolean {
        return authDataSource.getCurrentUser() != null
    }

    override suspend fun logout() {
        authDataSource.signOut()
    }
}

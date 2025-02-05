package com.yannick.data.datasource

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface FirebaseAuthDataSource {
    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser?
    fun getCurrentUser(): FirebaseUser?
    fun signOut()
}

class FirebaseAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : FirebaseAuthDataSource {

    override suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user
        } catch (e: Exception) {
            null
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}

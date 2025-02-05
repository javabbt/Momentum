package com.yannick.domain.repositories

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun signInWithGoogle(credential: AuthCredential): FirebaseUser?
    fun getCurrentUser(): FirebaseUser?
    suspend fun logout()
    fun isLoggedIn(): Boolean
}

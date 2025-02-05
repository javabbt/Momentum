package com.yannick.domain.usecases

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.yannick.domain.repositories.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(credential: AuthCredential): FirebaseUser? {
        return repository.signInWithGoogle(credential)
    }
}

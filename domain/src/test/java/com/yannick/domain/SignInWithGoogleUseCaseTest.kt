package com.yannick.domain

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.usecases.SignInWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInWithGoogleUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var useCase: SignInWithGoogleUseCase

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        useCase = SignInWithGoogleUseCase(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should return FirebaseUser when sign in is successful`() =
        mainCoroutineRule.runTest {
            // Arrange
            val credential = mockk<AuthCredential>()
            val expectedUser = mockk<FirebaseUser>()
            coEvery { repository.signInWithGoogle(credential) } returns expectedUser

            // Act
            val result = useCase(credential)

            // Assert
            assertEquals(expectedUser, result)
        }

    @Test
    fun `invoke should return null when sign in fails`() = mainCoroutineRule.runTest {
        // Arrange
        val credential = mockk<AuthCredential>()
        coEvery { repository.signInWithGoogle(credential) } returns null

        // Act
        val result = useCase(credential)

        // Assert
        assertEquals(null, result)
    }
}

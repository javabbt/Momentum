package com.yannick.featureauth

import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.core.utils.provideFakeCoroutinesDispatcherProvider
import com.yannick.domain.usecases.SignInWithGoogleUseCase
import com.yannick.featureauth.presentation.auth.AuthViewModel
import com.yannick.featureauth.presentation.auth.SideEffect
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore("This test is not working")
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private val dispatcherProvider =
        provideFakeCoroutinesDispatcherProvider(mainCoroutineRule.testDispatcher)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        viewModel = AuthViewModel(signInWithGoogleUseCase, dispatcherProvider)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle success updates user state`() = mainCoroutineRule.runTest {
        // Given
        val mockFirebaseUser: FirebaseUser = mockk()
        val mockCustomCredential: Credential = mockk(relaxUnitFun = true, relaxed = true)
        coEvery { signInWithGoogleUseCase(any()) } returns mockFirebaseUser
        testDispatcher.scheduler.advanceUntilIdle()
        // When & Then
        viewModel.uiState.test {
            // Verify initial state
            val initialState = awaitItem()
            assertNull(initialState.user)

            testDispatcher.scheduler.advanceUntilIdle()
            // Trigger sign in
            viewModel.signInWithGoogle(mockCustomCredential)

            // Verify updated state
            val updatedState = awaitItem()
            assertEquals(mockFirebaseUser, updatedState.user)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle with invalid credential type emits unexpected error`() =
        mainCoroutineRule.runTest {
            // Given
            val mockCustomCredential: CustomCredential = mockk {
                coEvery { type } returns "invalid.type"
                coEvery { data } returns Bundle()
            }

            // When & Then
            viewModel.sideEffects.test {
                viewModel.signInWithGoogle(mockCustomCredential)

                val effect = awaitItem()
                assert(effect is SideEffect.ShowUnexpectedError)
                assertEquals(
                    com.yannick.resources.R.string.unexpected_credential,
                    (effect as SideEffect.ShowUnexpectedError).msg,
                )
            }
        }

    @Test
    fun `signInWithGoogle failure emits error message`() = mainCoroutineRule.runTest {
        // Given
        val errorMessage = "Authentication failed"
        val mockCustomCredential: CustomCredential = mockk {
            coEvery { type } returns "type.googleapis.com/google.identity.signin.GoogleIdTokenCredential"
            coEvery { data } returns Bundle()
        }
        coEvery { signInWithGoogleUseCase(any()) } throws Exception(errorMessage)

        // When & Then
        viewModel.sideEffects.test {
            viewModel.signInWithGoogle(mockCustomCredential)

            val effect = awaitItem()
            assert(effect is SideEffect.ShowError)
            assertEquals(errorMessage, (effect as SideEffect.ShowError).msg)
        }
    }
}

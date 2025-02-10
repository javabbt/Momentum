package com.yannick.featureauth

import android.os.Bundle
import androidx.credentials.CustomCredential
import app.cash.turbine.test
import com.yannick.core.testutils.MainCoroutineRule
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
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()
    private val dispatcherProvider =
        provideFakeCoroutinesDispatcherProvider(mainCoroutineRule.testDispatcher)
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        viewModel = AuthViewModel(signInWithGoogleUseCase, dispatcherProvider)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signInWithGoogle with invalid credential type emits unexpected error`() =
        runTest {
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
}

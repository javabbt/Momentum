package com.yannick.featurehome.presentation.createchain

import android.net.Uri
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.core.utils.provideFakeCoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.utils.Result
import io.mockk.coEvery
import io.mockk.every
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateChainViewModelTest {

    private lateinit var viewModel: CreateChainViewModel
    private lateinit var firestoreService: FirestoreService
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val dispatcherProvider =
        provideFakeCoroutinesDispatcherProvider(mainCoroutineRule.testDispatcher)

    private val firebaseUser = mockk<FirebaseUser>(relaxed = true).apply {
        every { uid } returns "test-user-id"
        every { email } returns "test@test.com"
        every { displayName } returns "Test User"
    }

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        firestoreService = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        coEvery { authRepository.getCurrentUser() } returns firebaseUser

        viewModel = CreateChainViewModel(
            coroutinesDispatcherProvider = dispatcherProvider,
            fireStoreService = firestoreService,
            authRepository = authRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSubmit with empty fields emits error sideEffect`() = mainCoroutineRule.runTest {
        viewModel.sideEffects.test {
            viewModel.onSubmit("", "", null)

            val effect = awaitItem()
            assertTrue(effect is SideEffect.ShowUnexpectedError)
        }
    }

    @Test
    fun `onSubmit handles error case`() = runTest {
        val testUri = mockk<Uri>(relaxed = true, relaxUnitFun = true)

        val testException = Exception("Test error")
        coEvery { firestoreService.getFriendsOfUser(any()) } returns Result.Success(emptyList())
        coEvery { firestoreService.addChain(any(), any()) } throws testException

        viewModel.sideEffects.test {
            viewModel.onSubmit("Test Theme", "Test Caption", testUri)

            val effect = awaitItem()
            assertTrue(effect is SideEffect.ShowError)
            assertEquals(testException, (effect as SideEffect.ShowError).msg)
        }
    }
}

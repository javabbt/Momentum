package com.yannick.featurehome.presentation.profile

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.core.utils.CoroutinesDispatcherProvider
import com.yannick.data.services.FirestoreService
import com.yannick.domain.models.ProfileStats
import com.yannick.domain.repositories.AuthRepository
import com.yannick.domain.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var firestoreService: FirestoreService
    private lateinit var dispatcherProvider: CoroutinesDispatcherProvider
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        firestoreService = mockk(relaxed = true)
        dispatcherProvider = mockk {
            every { io } returns testDispatcher
            every { main } returns testDispatcher
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should update UI state with user data and stats when successful`() =
        mainCoroutineRule.runTest {
            // Given
            val mockUser = mockk<FirebaseUser> {
                every { uid } returns "123456789"
                every { displayName } returns "Test User"
                every { photoUrl } returns null
            }
            val mockStats = ProfileStats(
                chains = 5,
                friends = 10,
                streaks = 2,
                userName = "Test User",
                profilePicture = "http://photo.url",
                uid = "123",
            )

            coEvery { authRepository.getCurrentUser() } returns mockUser
            coEvery { firestoreService.getProfileOfUser(any()) } returns Result.Success(mockStats)

            // When
            viewModel = ProfileViewModel(authRepository, firestoreService, dispatcherProvider)

            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            viewModel.uiState.test {
                val finalState = awaitItem()
                assertEquals("Test User", finalState.displayName)
                assertEquals("123456", finalState.shortUserName)
                assertEquals(5, finalState.chains)
                assertEquals(10, finalState.friends)
                assertEquals(2, finalState.streaks)
            }
        }

    @Test
    fun `init should emit error side effect when stats fetch fails`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser> {
            every { uid } returns "123456789"
            every { displayName } returns "Test User"
            every { photoUrl } returns null
        }

        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { firestoreService.getProfileOfUser(any()) } returns Result.Error(Exception("Test error"))

        viewModel = ProfileViewModel(authRepository, firestoreService, dispatcherProvider)

        // When & Then
        viewModel.sideEffects.test {
            viewModel = ProfileViewModel(authRepository, firestoreService, dispatcherProvider)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assert(effect is SideEffect.ShowError)
            assertEquals("Test error", (effect as SideEffect.ShowError).msg)
        }
    }

    @Test
    fun `logout should emit LogoutDone side effect`() = mainCoroutineRule.runTest {
        // Given
        viewModel = ProfileViewModel(authRepository, firestoreService, dispatcherProvider)
        testDispatcher.scheduler.advanceUntilIdle()
        // When & Then
        viewModel.sideEffects.test {
            viewModel.logout()
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assert(effect is SideEffect.LogoutDone)
        }

        coVerify { authRepository.logout() }
    }
}

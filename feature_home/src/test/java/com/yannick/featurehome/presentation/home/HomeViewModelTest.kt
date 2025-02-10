package com.yannick.featurehome.presentation.home

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseUser
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.domain.repositories.AuthRepository
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
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have default values`() = mainCoroutineRule.runTest {
        // Given
        coEvery { authRepository.getCurrentUser() } returns null

        // When
        viewModel = HomeViewModel(authRepository)

        // Then
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.selectedIndex)
            assertEquals(null, initialState.user)
        }
    }

    @Test
    fun `should update user when getCurrentUser returns a user`() = mainCoroutineRule.runTest {
        // Given
        val mockUser = mockk<FirebaseUser>(relaxed = true, relaxUnitFun = true)
        coEvery { authRepository.getCurrentUser() } returns mockUser

        // When
        viewModel = HomeViewModel(authRepository)

        // Advance the dispatcher to complete the init block coroutine
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(mockUser, state.user)
        }
    }

    @Test
    fun `should update selectedIndex when onTabSelected is called`() = mainCoroutineRule.runTest {
        // Given
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = HomeViewModel(authRepository)

        // When
        viewModel.onTabSelected(2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedIndex)
        }
    }
}

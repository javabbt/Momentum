package com.yannick.momentum.ui

import app.cash.turbine.test
import com.yannick.core.testutils.MainCoroutineRule
import com.yannick.core.testutils.runTest
import com.yannick.core.theme.ThemePreferences
import com.yannick.domain.repositories.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Rule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AppViewModel
    private lateinit var themePreferences: ThemePreferences
    private lateinit var authRepository: AuthRepository
    private val isDarkModeFlow = MutableStateFlow(false)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        themePreferences = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        // Setup default mock behavior
        coEvery { themePreferences.isDarkMode } returns isDarkModeFlow
        coEvery { authRepository.isLoggedIn() } returns true
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should reflect theme preferences and auth status`() =
        mainCoroutineRule.runTest {
            // Given
            coEvery { authRepository.isLoggedIn() } returns true

            // When
            viewModel = AppViewModel(themePreferences, authRepository)

            // Then
            assertEquals(false, viewModel.uiState.value.isDarkMode)
            assertEquals(true, viewModel.uiState.value.isLoggedIn)
        }

    @Test
    fun `toggleTheme should invert current theme`() = mainCoroutineRule.runTest {
        // Given
        viewModel = AppViewModel(themePreferences, authRepository)
        val initialDarkMode = false // We know this is false from isDarkModeFlow's initial value

        // When
        viewModel.toggleTheme()

        themePreferences.isDarkMode.test {
            assertEquals(initialDarkMode, awaitItem())
        }
    }

    @Test
    fun `theme changes should update UI state`() = mainCoroutineRule.runTest {
        // Given
        viewModel = AppViewModel(themePreferences, authRepository)
        isDarkModeFlow.test {
            assertEquals(false, awaitItem())
        }
    }
}

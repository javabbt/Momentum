package com.yannick.featurehome.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomePageContainer(
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean = false,
    navController: NavController,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePage(
        onDarkModeToggle = onDarkModeToggle,
        isDarkMode = isDarkMode,
        selectedIndex = uiState.selectedIndex,
        onTabSelected = viewModel::onTabSelected,
        user = uiState.user,
        navController = navController,
    )
}

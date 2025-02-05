package com.yannick.momentum.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yannick.core.theme.AndroidMomentumAppTheme
import com.yannick.core.utils.AuthScreen
import com.yannick.core.utils.CreateChainScreen
import com.yannick.core.utils.HomeScreen
import com.yannick.core.utils.OnboardingScreen
import com.yannick.core.utils.SearchFriends
import com.yannick.core.utils.ViewPhotos
import com.yannick.featureauth.presentation.auth.AuthContainer
import com.yannick.featureauth.presentation.onboarding.OnboardingContainer
import com.yannick.featurehome.presentation.createchain.CreateChainContainer
import com.yannick.featurehome.presentation.home.HomePageContainer
import com.yannick.featurehome.presentation.searchfriends.SearchFriendsContainer
import com.yannick.featurehome.presentation.viewphotos.ViewPhotosContainer
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = koinViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val startDestination = if (uiState.isLoggedIn) HomeScreen else OnboardingScreen
            AndroidMomentumAppTheme(
                darkTheme = uiState.isDarkMode,
            ) {
                HomeContainer(
                    onDarkModeToggle = { viewModel.toggleTheme() },
                    isDarkMode = uiState.isDarkMode,
                    startDestination = startDestination,
                )
            }
        }
    }
}

@Composable
fun HomeContainer(
    onDarkModeToggle: () -> Unit = {},
    isDarkMode: Boolean = false,
    startDestination: String,
) {
    val navController = rememberNavController()
    HomeContainerNavGraph(navController, onDarkModeToggle, isDarkMode, startDestination)
}

object NavArgs {
    const val PHOTOS_LIST = "photosList"
}

@Composable
fun HomeContainerNavGraph(
    navController: NavHostController,
    onDarkModeToggle: () -> Unit,
    isDarkMode: Boolean = false,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composableWithSlideAnimation(
            route = AuthScreen,
        ) {
            AuthContainer(
                onDarkModeToggle = onDarkModeToggle,
                isDarkMode = isDarkMode,
                navController = navController,
            )
        }
        composableWithSlideAnimation(
            route = OnboardingScreen,
        ) {
            OnboardingContainer(
                onDarkModeToggle = onDarkModeToggle,
                isDarkMode = isDarkMode,
                onContinueClick = {
                    navController.navigate(AuthScreen)
                },
            )
        }
        composableWithSlideAnimation(
            route = HomeScreen,
        ) {
            HomePageContainer(
                onDarkModeToggle = onDarkModeToggle,
                isDarkMode = isDarkMode,
                navController = navController,
            )
        }
        composableWithSlideAnimation(
            route = CreateChainScreen,
        ) {
            CreateChainContainer(navController = navController)
        }
        composableWithSlideAnimation(
            route = ViewPhotos,
        ) { _ ->
            ViewPhotosContainer(
                navController = navController,
            )
        }
        composableWithSlideAnimation(
            route = SearchFriends,
        ) { _ ->
            SearchFriendsContainer(
                navController = navController,
            )
        }
    }
}

private fun NavGraphBuilder.composableWithSlideAnimation(
    route: String,
    content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500),
            ) + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500),
            ) + fadeOut(animationSpec = tween(500))
        },
    ) { navBackStackEntry -> // Add this parameter
        content(navBackStackEntry) // Pass it to the content
    }
}

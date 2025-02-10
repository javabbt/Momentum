package com.yannick.featurehome.presentation.searchfriends

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchFriendsContainer(
    navController: NavController,
) {
    val viewModel: SearchFriendViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.sideEffects.onEach {
            when (it) {
                is SideEffects.NothingFound -> {
                    Toast.makeText(
                        context,
                        com.yannick.resources.R.string.nothing_friends,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }.collect()
    }

    SearchFriends(navController = navController, onSearch = {
        viewModel.searchFriends(it)
    }, follow = { friend ->
        viewModel.followFriend(friend)
    }, isLoading = uiState.isLoading, friends = uiState.friends)
}

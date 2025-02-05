package com.yannick.featurehome.presentation.friends

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.yannick.core.utils.SearchFriends
import org.koin.androidx.compose.koinViewModel

@Composable
fun FriendsContainer(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: FriendsViewModel = koinViewModel()
    val pagingFriends = viewModel.friends.collectAsLazyPagingItems()
    val refresh = pagingFriends.loadState.refresh
    val append = pagingFriends.loadState.append

    FriendsPage(
        friends = pagingFriends,
        onUnfollow = { friend ->
            viewModel.unfollow(friend)
        },
        refresh,
        append,
        modifier,
        onSearchClick = {
            navController.navigate(SearchFriends)
        }
    )
}

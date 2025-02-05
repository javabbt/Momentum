package com.yannick.featurehome.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import com.yannick.core.ui.NothingFound
import com.yannick.domain.models.Friend
import com.yannick.resources.R

@Composable
fun FriendsPage(
    friends: LazyPagingItems<Friend>,
    onUnfollow: (Friend) -> Unit,
    refresh: LoadState,
    append: LoadState,
    modifier: Modifier,
    onSearchClick: () -> Unit,
) {
    if (friends.itemCount == 0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NothingFound(text = stringResource(R.string.nothing_found))
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
        ) {
            item {
                SearchBar(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                count = friends.itemCount,
                key = { index -> friends[index]?.userName ?: "Friend$index" },
            ) { index ->
                val friend = friends[index]
                if (friend != null) {
                    ProfileItem(
                        profileImage = friend.profilePicture,
                        userName = friend.userName,
                        onUnfollow = { onUnfollow(friend) },
                    )
                }
            }
        }
    }
    when {
        refresh is LoadState.Loading -> {
            CircularProgressIndicator()
        }

        append is LoadState.Loading -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ProfileItem(
    profileImage: String,
    userName: String,
    onUnfollow: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = profileImage,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )

            Text(
                text = userName,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Button(
            onClick = onUnfollow,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Unfollow")
        }
    }
}

@Composable
private fun SearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.search),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.search_friends),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

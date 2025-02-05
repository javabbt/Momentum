package com.yannick.featurehome.presentation.chains

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage
import com.yannick.core.theme.Blue
import com.yannick.core.ui.NothingFound
import com.yannick.domain.models.Chain
import com.yannick.domain.models.timeLeft
import com.yannick.resources.R

@Composable
fun ChainScreen(
    chains: LazyPagingItems<Chain>,
    refresh: LoadState,
    append: LoadState,
    modifier: Modifier = Modifier,
    onImageSelected: (String, Chain) -> Unit,
    loading: Boolean,
    onCardClicked: (List<String>) -> Unit = {},
) {
    if (chains.itemCount == 0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NothingFound(stringResource(R.string.no_chains))
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            item {
                Text(
                    stringResource(R.string.active_chains),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            items(
                chains.itemCount,
                key = { index -> chains[index]?.id ?: index },
            ) { chain ->
                chains.itemSnapshotList[chain]?.let { chainItem ->
                    ChainItem(chainItem, onImageSelected = {
                        onImageSelected(it, chainItem)
                    }, loading = loading, onCardClicked = onCardClicked)
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
}

@Composable
fun ChainItem(
    chain: Chain,
    onImageSelected: (String) -> Unit = {},
    loading: Boolean,
    onCardClicked: (List<String>) -> Unit = {},
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            onImageSelected(it.toString())
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCardClicked(chain.photos.sortedByDescending { it.timestamp }.map { it.photoUrl })
            }
            .padding(vertical = 14.dp, horizontal = 18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Header row with title and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = chain.theme,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(
                            R.string.participants_and_streak,
                            chain.participants.size,
                            chain.photos.size,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Text(
                    text = stringResource(R.string.hours_left, chain.timeLeft()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Photo placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                Row {
                    AsyncImage(
                        model = chain.photos.firstOrNull()?.photoUrl ?: "",
                        contentDescription = stringResource(R.string.chain_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(.8f)
                            .clip(
                                RoundedCornerShape(
                                    8.dp,
                                ),
                            ),
                    )
                    Spacer(modifier = Modifier.weight(.1f))
                    Box(
                        modifier = Modifier
                            .weight(.8f)
                            .height(160.dp)
                            .background(Blue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (!loading) {
                                    galleryLauncher.launch("image/*")
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(onClick = {
                            if (!loading) {
                                galleryLauncher.launch("image/*")
                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = null,
                                tint = Blue,
                                modifier = Modifier
                                    .size(30.dp)
                                    .alpha(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

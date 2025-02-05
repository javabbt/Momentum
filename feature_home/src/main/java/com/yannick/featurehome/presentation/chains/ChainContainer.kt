package com.yannick.featurehome.presentation.chains

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.yannick.core.utils.ViewPhotos
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChainContainer(modifier: Modifier = Modifier, navController: NavController) {
    val chainViewModel: ChainsViewModel = koinViewModel()
    val chains = chainViewModel.chains.collectAsLazyPagingItems()
    val refresh = chains.loadState.refresh
    val append = chains.loadState.append

    val uiState by chainViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(chainViewModel) {
        chainViewModel.sideEffect.onEach {
            when (it) {
                is SideEffect.UploadDone -> {
                    Toast.makeText(
                        context,
                        com.yannick.resources.R.string.image_uploaded,
                        Toast.LENGTH_SHORT,
                    ).show()
                    chainViewModel.reloadChains()
                }
            }
        }.collect()
    }

    if (uiState.isLoading) {
        Toast.makeText(context, com.yannick.resources.R.string.uploading, Toast.LENGTH_SHORT).show()
    }

    ChainScreen(
        chains = chains,
        refresh = refresh,
        append = append,
        modifier = modifier,
        onImageSelected = { image, chain -> chainViewModel.onImageSelected(image, chain) },
        loading = uiState.isLoading,
        onCardClicked = { photos ->
            chainViewModel.setPhotos(photos)
            navController.navigate(ViewPhotos)
        },
    )
}

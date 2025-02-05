package com.yannick.featurehome.presentation.createchain

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CreateChainContainer(navController: NavHostController) {
    val viewModel: CreateChainViewModel = koinViewModel()
    val uiModel by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.sideEffects.onEach {
            when (it) {
                is SideEffect.ShowError -> {
                    Toast.makeText(navController.context, it.msg.message, Toast.LENGTH_SHORT).show()
                }

                is SideEffect.ShowUnexpectedError -> {
                    Toast.makeText(navController.context, it.msg, Toast.LENGTH_SHORT).show()
                }

                is SideEffect.UploadDone -> {
                    Toast.makeText(
                        navController.context,
                        com.yannick.resources.R.string.upload_done,
                        Toast.LENGTH_SHORT,
                    ).show()
                    navController.popBackStack()
                }
            }
        }.collect()
    }
    CreateChain(
        navController = navController,
        onImageSelected = { viewModel.onImageSelected(it) },
        uriFile = uiModel.photo?.let { Uri.parse(it) },
        onSubmit = { theme, caption, uri ->
            coroutineScope.launch {
                viewModel.onSubmit(theme, caption, uri)
            }
        },
        loading = uiModel.loading,
    )
}

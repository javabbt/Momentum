package com.yannick.featurehome.presentation.viewphotos

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel

@Composable
fun ViewPhotosContainer(navController: NavController) {
    val viewModel: ViewPhotosViewModel = koinViewModel()
    ViewPhotos(navController = navController, photos = viewModel.getPhotos())
}

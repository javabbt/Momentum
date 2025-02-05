package com.yannick.featurehome.presentation.shared

import androidx.compose.runtime.mutableStateOf

class PhotosHolder {
    var photos = mutableStateOf<List<String>>(emptyList())
}

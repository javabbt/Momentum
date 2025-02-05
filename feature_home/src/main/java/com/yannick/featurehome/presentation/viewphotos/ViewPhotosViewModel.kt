package com.yannick.featurehome.presentation.viewphotos

import androidx.lifecycle.ViewModel
import com.yannick.featurehome.presentation.shared.PhotosHolder

class ViewPhotosViewModel(
    private val holder: PhotosHolder,
) : ViewModel() {
    fun getPhotos() = holder.photos.value
}

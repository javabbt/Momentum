package com.yannick.domain.utils

import androidx.annotation.StringRes

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val output: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data class UnexpectedError(@StringRes val exception: Int) : Result<Nothing>()
}

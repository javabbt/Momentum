package com.yannick.domain.models

data class ProfileStats(
    val friends: Int,
    val chains: Int,
    val streaks: Int,
    val userName: String,
    val profilePicture: String,
    val uid: String,
)

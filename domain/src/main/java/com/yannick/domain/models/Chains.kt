package com.yannick.domain.models

import org.joda.time.Hours
import org.joda.time.LocalDateTime

data class Chain(
    val id: String = "",
    val theme: String = "",
    val createdBy: String = "",
    val participants: List<String> = emptyList(),
    val deadline: String = "",
    val streak: Int = 0,
    val photos: List<ChainPhoto> = emptyList(),
    val chainStatus: String = "",
    val caption: String = "",
)

fun Chain.timeLeft(): String {
    val currentDate = LocalDateTime.parse(deadline)
    val left = Hours.hoursBetween(LocalDateTime.now(), currentDate).hours
    return left.toString()
}

data class ChainPhoto(
    val id: String = "",
    val chainId: String = "",
    val userId: String = "",
    val photoUrl: String = "",
    val timestamp: String = "",
)

enum class ChainStatus {
    ACTIVE,
    BROKEN,
    COMPLETED,
}

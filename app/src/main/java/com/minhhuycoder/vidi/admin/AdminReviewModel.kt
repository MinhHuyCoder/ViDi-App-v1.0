package com.minhhuycoder.vidi.admin

data class AdminReviewModel(
    val reviewId: String = "",
    val placeId: String = "",
    val userId: String = "",
    val username: String = "",
    val rating: Double = 0.0,
    val comment: String = "",
    val status: String = "visible"
)
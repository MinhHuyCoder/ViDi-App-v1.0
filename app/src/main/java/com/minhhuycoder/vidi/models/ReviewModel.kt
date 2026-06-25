package com.minhhuycoder.vidi.models

import com.google.firebase.Timestamp

data class ReviewModel(
    val reviewId: String = "",
    val placeId: String = "",
    val userId: String = "",
    val username: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Timestamp? = null,
    val status: String = "visible"
)
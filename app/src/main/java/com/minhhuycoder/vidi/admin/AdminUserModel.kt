package com.minhhuycoder.vidi.admin

data class AdminUserModel(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "user",
    var status: String = "active",
    val joinDate: String = "Chưa có",
    var totalReviews: Int = 0,
    var wishlistCount: Int = 0
)
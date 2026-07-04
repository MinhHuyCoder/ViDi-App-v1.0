package com.minhhuycoder.vidi.models

data class PlaceModel(

    val placeId: String = "",

    val name: String = "",

    val category: String = "",

    val address: String = "",

    val description: String = "",

    val imageUrl: String = "",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val openTime: String = "",

    val closeTime: String = "",

    val rating: Double = 0.0,

    val reviewCount: Int = 0,

    val status: Boolean = true

)
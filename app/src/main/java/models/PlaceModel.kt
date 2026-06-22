package models

data class PlaceModel(
    val placeId: String = "",
    val name: String = "",
    val address: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val ratingAverage: Double = 5.0
)
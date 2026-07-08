package com.minhhuycoder.vidi.core

import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.models.ReviewModel
import kotlinx.coroutines.tasks.await
/**
 * ReviewRepository - Xử lý dữ liệu từ Firebase Firestore sử dụng Coroutines
 */
class ReviewRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("reviews")

    /**
     * Lấy danh sách đánh giá theo mã địa điểm (placeId)
     * Trả về List<ReviewModel> hoặc emptyList() nếu có lỗi
     */
    suspend fun getReviewsByPlace(placeId: String): List<ReviewModel> {
        return try {
            val snapshot = reviewsCollection
                .whereEqualTo("placeId", placeId)
                .whereEqualTo("status", "visible")
                .get()
                .await() // Sử dụng kotlinx-coroutines-play-services để await Task
            
            snapshot.toObjects(ReviewModel::class.java)
        } catch (e: Exception) {
            // Log lỗi nếu cần thiết và trả về danh sách trống
            emptyList()
        }
    }
    suspend fun addReview(review: ReviewModel): Boolean {

        return try {

            reviewsCollection
                .add(review)
                .await()

            true

        } catch (e: Exception) {

            false

        }
    }
}

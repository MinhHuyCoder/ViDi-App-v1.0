package com.minhhuycoder.vidi.core

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.minhhuycoder.vidi.models.PlaceModel

class PlaceRepository {
    // Dùng by lazy để trì hoãn việc khởi tạo Firebase cho đến khi gọi hàm getPlaces()
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun getPlaces(): List<PlaceModel> {
        return try {
            val snapshot = db.collection(Const.COLLECTION_PLACES).get().await()
            snapshot.toObjects(PlaceModel::class.java)
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Lỗi khi lấy dữ liệu: ${e.message}")
            emptyList()
        }
    }

    // === THÊM: Hàm lấy thông tin chi tiết của 1 quán theo ID phục vụ màn hình Detail ===
    suspend fun getPlaceDetail(placeId: String): PlaceModel? {
        return try {
            val snapshot = db.collection(Const.COLLECTION_PLACES).document(placeId).get().await()
            snapshot.toObject(PlaceModel::class.java)
        } catch (e: Exception) {
            Log.e("PlaceRepository", "Lỗi khi lấy chi tiết địa điểm: ${e.message}")
            null
        }
    }
}
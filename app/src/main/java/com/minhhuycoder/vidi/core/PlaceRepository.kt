package core.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.core.Const
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
}
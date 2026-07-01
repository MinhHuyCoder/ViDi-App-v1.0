package com.minhhuycoder.vidi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.minhhuycoder.vidi.core.PlaceRepository
import com.minhhuycoder.vidi.models.PlaceModel // Đảm bảo folder 'models' nằm cùng cấp với folder 'viewmodel'
import kotlinx.coroutines.Dispatchers

class PlaceViewModel : ViewModel() {
    private val repository = PlaceRepository()

    // Khai báo tường minh kiểu dữ liệu là List<PlaceModel>
    val places: LiveData<List<PlaceModel>> = liveData(Dispatchers.IO) {
        try {
            val data = repository.getPlaces()
            emit(data)
        } catch (e: Exception) {
            android.util.Log.e("Loi_Firebase", "Chết ở đây: ${e.message}") // Thêm dòng này
            emit(emptyList())
        }
    }
}
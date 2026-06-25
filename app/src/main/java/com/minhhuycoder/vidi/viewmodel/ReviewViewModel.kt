package com.minhhuycoder.vidi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minhhuycoder.vidi.core.ReviewRepository
import com.minhhuycoder.vidi.models.ReviewModel
import kotlinx.coroutines.launch

/**
 * ReviewViewModel - Quản lý trạng thái dữ liệu đánh giá cho UI
 */
class ReviewViewModel : ViewModel() {

    // Khởi tạo Repository trực tiếp (theo yêu cầu không dùng DI)
    private val repository = ReviewRepository()

    // LiveData chứa danh sách đánh giá để View quan sát (Observe)
    private val _reviews = MutableLiveData<List<ReviewModel>>()
    val reviews: LiveData<List<ReviewModel>> get() = _reviews

    // LiveData trạng thái loading (tùy chọn nhưng cần thiết cho UX)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    /**
     * Tải danh sách đánh giá từ Repository theo placeId
     */
    fun loadReviews(placeId: String) {
        _isLoading.value = true
        
        // Chạy trong viewModelScope để tự động hủy khi ViewModel bị clear
        viewModelScope.launch {
            val result = repository.getReviewsByPlace(placeId)
            _reviews.postValue(result)
            _isLoading.postValue(false)
        }
    }
}

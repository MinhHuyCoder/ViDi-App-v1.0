package com.minhhuycoder.vidi.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.minhhuycoder.vidi.core.PlaceRepository
import com.minhhuycoder.vidi.models.PlaceModel

class PlaceViewModel : ViewModel() {
    private val repository = PlaceRepository()

    // Khai báo tường minh kiểu dữ liệu là List<PlaceModel>
    private val _places = MutableLiveData<List<PlaceModel>>()
    val places: LiveData<List<PlaceModel>> = _places

    fun loadPlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repository.getPlaces()
                _places.postValue(data)
            } catch (e: Exception) {
                _places.postValue(emptyList())
            }
        }
    }

    init {
        loadPlaces()
    }
}
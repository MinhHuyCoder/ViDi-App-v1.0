package com.minhhuycoder.vidi.wishlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.databinding.ActivityWishlistBinding
import com.minhhuycoder.vidi.models.PlaceModel // Sử dụng trực tiếp model gốc của nhóm

class WishlistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var adapter: WishlistAdapter
    private var allFavoritePlaces = listOf<PlaceModel>() // Lưu danh sách gốc phục vụ tìm kiếm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        checkUserAndLoadData()
    }

    private fun setupRecyclerView() {
        adapter = WishlistAdapter(emptyList()) { place ->
            // Thực hiện hành động xóa khỏi wishlist khi click vào nút Tim
            removePlaceFromWishlist(place.placeId)
        }
        binding.rvWishlist.adapter = adapter
    }

    private fun checkUserAndLoadData() {
        // Sử dụng ID cứng "user_test_huy" nếu chưa tích hợp module Đăng nhập
        val currentUserId = auth.currentUser?.uid ?: "user_test_huy"

        // Lắng nghe Realtime (SnapshotListener) bảng wishlist lọc theo đúng userId hiện tại
        db.collection("wishlist")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    // Nếu bảng trống, kích hoạt cụm thông báo Empty Message
                    updateUI(emptyList())
                    return@addSnapshotListener
                }

                // Gom nhanh toàn bộ list placeId mà user này đã thả tim
                val placeIds = snapshot.toObjects(WishlistModel::class.java).map { it.placeId }

                if (placeIds.isEmpty()) {
                    updateUI(emptyList())
                } else {
                    // Chọc sang bảng places của Hiếu lấy thông tin chi tiết quán hiển thị
                    fetchPlacesDetails(placeIds)
                }
            }
    }

    private fun fetchPlacesDetails(placeIds: List<String>) {
        db.collection("places")
            .whereIn("placeId", placeIds)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = querySnapshot.toObjects(PlaceModel::class.java)

                allFavoritePlaces = placesList
                updateUI(placesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi đồng bộ dữ liệu địa điểm!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removePlaceFromWishlist(placeId: String) {
        val currentUserId = auth.currentUser?.uid ?: "user_test_huy"
        // Áp dụng mẹo Document ID: userId_placeId gộp chuỗi chống lag tuyệt đối
        val docId = "${currentUserId}_${placeId}"

        db.collection("wishlist").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show()
            }
    }

    // Xử lý thanh tìm kiếm Realtime trên EditText (etSearch)
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim().lowercase()
            val filteredList = if (query.isEmpty()) {
                allFavoritePlaces
            } else {
                allFavoritePlaces.filter { it.name.lowercase().contains(query) }
            }
            adapter.submitList(filteredList)
        }
    }

    // Hàm đồng bộ trạng thái ẩn/hiện cụm llEmptyMessage của Huy khi dữ liệu biến động
    private fun updateUI(list: List<PlaceModel>) {
        if (list.isEmpty()) {
            binding.llEmptyMessage.visibility = View.VISIBLE // Hiện thông báo rỗng
            binding.rvWishlist.visibility = View.GONE        // Ẩn danh sách RecyclerView
        } else {
            binding.llEmptyMessage.visibility = View.GONE     // Ẩn thông báo rỗng
            binding.rvWishlist.visibility = View.VISIBLE     // Hiện danh sách RecyclerView
            adapter.submitList(list)
        }
    }
}
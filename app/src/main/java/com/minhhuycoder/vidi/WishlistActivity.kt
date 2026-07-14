package com.minhhuycoder.vidi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.databinding.ActivityWishlistBinding
import com.minhhuycoder.vidi.models.PlaceModel // Sử dụng trực tiếp model gốc của nhóm
import android.content.Intent
import com.minhhuycoder.vidi.DetailActivity
import com.minhhuycoder.vidi.MainActivity
import android.util.Log



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
        binding.bottomNavigation.selectedItemId = R.id.nav_favorite

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true

                }

                R.id.nav_favorite -> {

                    startActivity(
                        Intent(
                            this,
                            WishlistActivity::class.java
                        )
                    )

                    true
                }

                R.id.nav_notifications -> {

                    Toast.makeText(
                        this,
                        "Đang phát triển",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                R.id.nav_profile -> {

                    startActivity(
                        Intent(
                            this,
                            ProfileActivity::class.java
                        )
                    )

                    true
                }

                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WishlistAdapter(
            emptyList(),

            onItemClicked = { place ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("PLACE_ID", place.placeId)
                startActivity(intent)
            },

            onHeartClicked = { place ->
                removePlaceFromWishlist(place.placeId)
            }
        )
        binding.rvWishlist.adapter = adapter
    }

    private fun checkUserAndLoadData() {
        // Sử dụng ID cứng "user_test_huy" nếu chưa tích hợp module Đăng nhập
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentUserId = currentUser.uid

        // Lắng nghe Realtime (SnapshotListener) bảng wishlist lọc theo đúng userId hiện tại
        db.collection("favorites")

            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->

                Log.d("WISHLIST", "favorites = ${snapshot?.size()}")

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

                Log.d("WISHLIST", "placeIds = $placeIds")
                val placesList = querySnapshot.toObjects(PlaceModel::class.java)

                allFavoritePlaces = placesList
                updateUI(placesList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi đồng bộ dữ liệu địa điểm!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removePlaceFromWishlist(placeId: String) {

        val currentUser = auth.currentUser ?: return

        db.collection("favorites")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("placeId", placeId)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    document.reference.delete()
                }

                Toast.makeText(
                    this,
                    "Đã xóa khỏi danh sách yêu thích!",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Xóa thất bại!",
                    Toast.LENGTH_SHORT
                ).show()

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
            binding.rvWishlist.visibility = View.VISIBLE
            Log.d("WISHLIST", "submit = ${list.size}")// Hiện danh sách RecyclerView
            adapter.submitList(list)
        }
    }
}
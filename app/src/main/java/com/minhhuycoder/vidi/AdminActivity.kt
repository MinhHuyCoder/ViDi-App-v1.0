package com.minhhuycoder.vidi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.admin.AdminPlaceAdapter
import com.minhhuycoder.vidi.admin.AdminReviewModel
import com.minhhuycoder.vidi.models.PlaceModel

class AdminActivity : AppCompatActivity() {

    private lateinit var rvPlaces: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var tvTotalPlaces: TextView
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvTotalReviews: TextView
    private lateinit var fabAddPlace: View
    private lateinit var tabUsers: View
    private lateinit var placeAdapter: AdminPlaceAdapter

    private val db = FirebaseFirestore.getInstance()
    private val placeList = mutableListOf<PlaceModel>()

    // Lưu danh sách gốc để tìm kiếm không làm mất dữ liệu
    private val allPlaces = mutableListOf<PlaceModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()
        setupRecyclerView()
        setupSearch()
        setupEvents()
    }

    override fun onResume() {
        super.onResume()

        // Khi quay lại từ màn thêm/sửa thì tải lại danh sách mới nhất
        loadPlacesFromFirebase()
    }

    private fun initViews() {
        rvPlaces = findViewById(R.id.rvPlaces)
        etSearch = findViewById(R.id.etSearch)
        tvTotalPlaces = findViewById(R.id.tvTotalPlaces)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvTotalReviews = findViewById(R.id.tvTotalReviews)
        fabAddPlace = findViewById(R.id.fabAddPlace)
        tabUsers = findViewById(R.id.tabUsers)
    }

    private fun setupRecyclerView() {
        placeAdapter = AdminPlaceAdapter(
            placeList,
            onEditClick = { place ->
                // Mở màn sửa và gửi placeId sang AdminEditPlaceActivity
                val intent = Intent(this, AdminEditPlaceActivity::class.java)
                intent.putExtra("placeId", place.placeId)
                startActivity(intent)
            },
            onDeleteClick = { place ->
                // Bấm xóa thì hiện hộp thoại xác nhận trước
                showDeleteConfirmDialog(place)
            },
            onLoadReviewClick = { place ->
                // Chỉ khi bấm mở rộng địa điểm mới load review
                loadReviewsForPlace(place)
            },
            onHideReviewClick = { review ->
                // Ẩn / hiện review
                toggleReviewStatus(review)
            },
            onDeleteReviewClick = { review ->
                // Xóa review
                showDeleteReviewConfirmDialog(review)
            }
        )

        rvPlaces.layoutManager = LinearLayoutManager(this)
        rvPlaces.adapter = placeAdapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterPlaces(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setupEvents() {
        // Bấm nút thêm địa điểm thì mở màn thêm địa điểm
        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AdminEditPlaceActivity::class.java)
            startActivity(intent)
        }

        // Bấm tab Người dùng thì mở màn quản lý người dùng
        tabUsers.setOnClickListener {
            val intent = Intent(this, AdminUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPlacesFromFirebase() {
        // Lấy danh sách địa điểm thật từ collection places
        db.collection("places")
            .get()
            .addOnSuccessListener { result ->
                allPlaces.clear()

                for (document in result) {
                    val place = document.toObject(PlaceModel::class.java)

                    // Gắn document.id vào placeId để sửa / xóa đúng document
                    allPlaces.add(
                        place.copy(placeId = document.id)
                    )
                }

                val keyword = etSearch.text.toString().trim()

                // Nếu đang tìm kiếm thì giữ kết quả lọc
                if (keyword.isEmpty()) {
                    placeAdapter.updateData(allPlaces)
                } else {
                    filterPlaces(keyword)
                }

                updateSummary(allPlaces)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tải địa điểm: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun filterPlaces(keyword: String) {
        val searchText = keyword.trim().lowercase()

        // Nếu ô tìm kiếm rỗng thì hiện toàn bộ địa điểm
        if (searchText.isEmpty()) {
            placeAdapter.updateData(allPlaces)
            return
        }

        val filteredList = allPlaces.filter { place ->
            place.name.lowercase().contains(searchText) ||
                    place.category.lowercase().contains(searchText) ||
                    place.address.lowercase().contains(searchText)
        }

        placeAdapter.updateData(filteredList)
    }

    private fun updateSummary(list: List<PlaceModel>) {
        // Cập nhật số địa điểm thật
        tvTotalPlaces.text = list.size.toString()

        // User sẽ quản lý ở AdminUserActivity, tạm để 0 tại màn này
        tvTotalUsers.text = "0"

        // Tổng review lấy từ reviewCount của từng địa điểm
        val totalReviews = list.sumOf { it.reviewCount }
        tvTotalReviews.text = totalReviews.toString()
    }

    private fun loadReviewsForPlace(place: PlaceModel) {
        if (place.placeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Place ID", Toast.LENGTH_SHORT).show()
            return
        }

        loadReviewsForPlaceId(place.placeId)
    }

    private fun loadReviewsForPlaceId(placeId: String) {
        // Lấy review đúng theo placeId
        db.collection("reviews")
            .whereEqualTo("placeId", placeId)
            .get()
            .addOnSuccessListener { result ->
                val reviewList = mutableListOf<AdminReviewModel>()

                for (document in result) {
                    val review = AdminReviewModel(
                        reviewId = document.id,
                        placeId = document.getString("placeId") ?: "",
                        userId = document.getString("userId") ?: "",
                        username = document.getString("username") ?: "Người dùng",
                        rating = document.getDouble("rating") ?: 0.0,
                        comment = document.getString("comment") ?: "",
                        status = document.getString("status") ?: "visible"
                    )

                    reviewList.add(review)
                }

                placeAdapter.setReviewsForPlace(placeId, reviewList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tải review: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun toggleReviewStatus(review: AdminReviewModel) {
        if (review.reviewId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Review ID", Toast.LENGTH_SHORT).show()
            return
        }

        val newStatus = if (review.status == "hidden") {
            "visible"
        } else {
            "hidden"
        }

        // Admin chỉ ẩn / hiện review
        db.collection("reviews")
            .document(review.reviewId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã cập nhật trạng thái review", Toast.LENGTH_SHORT).show()

                // Load lại review và tính lại rating/reviewCount
                loadReviewsForPlaceId(review.placeId)
                updatePlaceRatingAndReviewCount(review.placeId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi cập nhật review: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showDeleteReviewConfirmDialog(review: AdminReviewModel) {
        AlertDialog.Builder(this)
            .setTitle("Xóa review")
            .setMessage("Bạn có chắc muốn xóa review này không?")
            .setPositiveButton("Xóa") { _, _ ->
                deleteReview(review)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deleteReview(review: AdminReviewModel) {
        if (review.reviewId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Review ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Xóa review khỏi collection reviews
        db.collection("reviews")
            .document(review.reviewId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Đã xóa review", Toast.LENGTH_SHORT).show()

                // Load lại review và cập nhật rating/reviewCount
                loadReviewsForPlaceId(review.placeId)
                updatePlaceRatingAndReviewCount(review.placeId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi xóa review: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updatePlaceRatingAndReviewCount(placeId: String) {
        if (placeId.isEmpty()) {
            return
        }

        // Tính rating và reviewCount từ collection reviews
        db.collection("reviews")
            .whereEqualTo("placeId", placeId)
            .get()
            .addOnSuccessListener { result ->
                var totalRating = 0.0
                var visibleReviewCount = 0

                for (document in result) {
                    val status = document.getString("status") ?: "visible"

                    // Chỉ tính review đang hiện
                    if (status == "visible") {
                        val rating = document.getDouble("rating") ?: 0.0
                        totalRating += rating
                        visibleReviewCount++
                    }
                }

                val averageRating = if (visibleReviewCount == 0) {
                    0.0
                } else {
                    totalRating / visibleReviewCount
                }

                // Làm tròn 1 chữ số thập phân
                val roundedRating = Math.round(averageRating * 10.0) / 10.0

                val updateData = hashMapOf<String, Any>(
                    "rating" to roundedRating,
                    "reviewCount" to visibleReviewCount
                )

                // Cập nhật rating và reviewCount sau khi xử lý review
                db.collection("places")
                    .document(placeId)
                    .update(updateData)
                    .addOnSuccessListener {
                        loadPlacesFromFirebase()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Lỗi cập nhật rating: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tính lại review: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showDeleteConfirmDialog(place: PlaceModel) {
        AlertDialog.Builder(this)
            .setTitle("Xóa địa điểm")
            .setMessage("Bạn có chắc muốn xóa \"${place.name}\" không?")
            .setPositiveButton("Xóa") { _, _ ->
                deletePlace(place)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun deletePlace(place: PlaceModel) {
        if (place.placeId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy Place ID", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo batch để xóa nhiều document cùng lúc
        val batch = db.batch()

        // Bước 1: tìm toàn bộ review thuộc địa điểm này
        db.collection("reviews")
            .whereEqualTo("placeId", place.placeId)
            .get()
            .addOnSuccessListener { reviewResult ->

                for (reviewDoc in reviewResult) {
                    batch.delete(reviewDoc.reference)
                }

                // Bước 2: tìm toàn bộ wishlist thuộc địa điểm này
                db.collection("favorites")
                    .whereEqualTo("placeId", place.placeId)
                    .get()
                    .addOnSuccessListener { wishlistResult ->

                        for (wishlistDoc in wishlistResult) {
                            batch.delete(wishlistDoc.reference)
                        }

                        // Bước 3: xóa document địa điểm chính
                        val placeRef = db.collection("places").document(place.placeId)
                        batch.delete(placeRef)

                        // Bước 4: thực thi toàn bộ thao tác xóa
                        batch.commit()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Đã xóa ${place.name}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadPlacesFromFirebase()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Lỗi xóa địa điểm: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Lỗi tải wishlist: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tải reviews: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
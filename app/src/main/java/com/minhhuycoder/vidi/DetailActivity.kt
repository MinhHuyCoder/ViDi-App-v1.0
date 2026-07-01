package com.minhhuycoder.vidi

import android.os.Bundle
import android.widget.Toast // === THÊM: Để hiển thị thông báo lỗi nếu có ===
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // === THÊM: Để chạy Coroutines gọi Repository ===
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide // === THÊM: Để load ảnh online từ URL ===
import com.minhhuycoder.vidi.core.ReviewAdapter
import com.minhhuycoder.vidi.databinding.ActivityDetailBinding
import com.minhhuycoder.vidi.viewmodel.ReviewViewModel
import com.minhhuycoder.vidi.models.PhotoModel
import com.minhhuycoder.vidi.core.PhotoAdapter
import com.minhhuycoder.vidi.core.PlaceRepository // === THÊM: Kết nối với tầng dữ liệu quán ===
import kotlinx.coroutines.launch

/**
 * DetailActivity - Hiển thị chi tiết địa điểm và xử lý danh sách đánh giá từ Firestore
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: ReviewViewModel
    private val reviewAdapter by lazy { ReviewAdapter() }

    // === THÊM: Khai báo đối tượng Repository xử lý logic lấy dữ liệu quán ===
    private val placeRepository by lazy { PlaceRepository() }

    private val photoList = listOf(
        PhotoModel("https://picsum.photos/600/600?1"),
        PhotoModel("https://picsum.photos/600/600?2"),
        PhotoModel("https://picsum.photos/600/600?3"),
        PhotoModel("https://picsum.photos/600/600?4"),
        PhotoModel("https://picsum.photos/600/600?5")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === THÊM: Xử lý nút quay lại giao diện chính ===
        binding.btnBack.setOnClickListener { finish() }

        // 1. Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        setupRecyclerView()
        setupAlbumRecyclerView()
        observeViewModel()

        // 2. Lấy placeId từ Intent (giả sử truyền từ màn hình trước)
        // === SỬA: Loại bỏ chuỗi mặc định, lấy ID thực và kiểm tra tính hợp lệ ===
        val placeId = intent.getStringExtra("PLACE_ID")

        if (!placeId.isNullOrEmpty()) {
            // === THÊM: Gọi hàm kéo dữ liệu chi tiết quán đổ lên các ID XML ===
            loadPlaceDetail(placeId)

            // 3. Gọi hàm tải dữ liệu từ Firestore thông qua ViewModel
            viewModel.loadReviews(placeId)
        } else {
            Toast.makeText(this, "Không nhận được mã địa điểm từ màn hình chính!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // === THÊM: Hàm logic lấy thông tin quán và hiển thị lên UI ===
    private fun loadPlaceDetail(placeId: String) {
        lifecycleScope.launch {
            val place = placeRepository.getPlaceDetail(placeId)
            if (place != null) {
                // Đổ dữ liệu chữ bám sát chính xác các ID trong file XML của bạn
                binding.tvPlaceName.text = place.name
                binding.tvCategory.text = "☕ ${place.category}"
                binding.tvAddress.text = place.address
                binding.tvDescription.text = place.description

                // Binding dữ liệu điểm số trung bình (ratingAverage) từ hợp đồng dữ liệu
                binding.tvRating.text = "⭐ ${String.format("%.1f", place.ratingAverage)} (${place.reviewCount} đánh giá)"
                binding.tvAverageRating.text = String.format("%.1f", place.ratingAverage)

                binding.tvOpen.text = if (place.status) {
                    "🟢 Mở đến ${place.closeTime}"
                } else {
                    "🔴 Đã đóng"
                }

                // Dùng Glide tải ảnh Banner to lên đầu màn hình
                Glide.with(this@DetailActivity)
                    .load(place.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(binding.ivPlaceImage)
            } else {
                Toast.makeText(this@DetailActivity, "Không tìm thấy dữ liệu địa điểm này trên Firestore!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@DetailActivity)
            adapter = reviewAdapter
            isNestedScrollingEnabled = false // Tránh xung đột cuộn với NestedScrollView
        }
    }

    private fun setupAlbumRecyclerView() {
        binding.rvAlbum.apply {
            layoutManager = LinearLayoutManager(
                this@DetailActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = PhotoAdapter(photoList)
        }
    }

    /**
     * Quan sát LiveData từ ViewModel để cập nhật UI tự động
     */
    private fun observeViewModel() {
        // Cập nhật danh sách RecyclerView khi có dữ liệu mới
        viewModel.reviews.observe(this) { list ->
            reviewAdapter.submitList(list)
        }

        // Hiển thị/Ẩn loading (nếu bạn muốn thêm ProgressBar)
        viewModel.isLoading.observe(this) { isLoading ->
            // Ví dụ: binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
}
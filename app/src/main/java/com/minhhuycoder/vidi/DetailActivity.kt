package com.minhhuycoder.vidi

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.minhhuycoder.vidi.core.ReviewAdapter
import com.minhhuycoder.vidi.databinding.ActivityDetailBinding
import com.minhhuycoder.vidi.viewmodel.ReviewViewModel
import com.minhhuycoder.vidi.models.PhotoModel
import com.minhhuycoder.vidi.core.PhotoAdapter

/**
 * DetailActivity - Hiển thị chi tiết địa điểm và xử lý danh sách đánh giá từ Firestore
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var viewModel: ReviewViewModel
    private val reviewAdapter by lazy { ReviewAdapter() }

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

        // 1. Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        setupRecyclerView()
        setupAlbumRecyclerView()
        observeViewModel()

        // 2. Lấy placeId từ Intent (giả sử truyền từ màn hình trước)
        val placeId = intent.getStringExtra("PLACE_ID") ?: "default_place_id"
        
        // 3. Gọi hàm tải dữ liệu từ Firestore thông qua ViewModel
        viewModel.loadReviews(placeId)
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

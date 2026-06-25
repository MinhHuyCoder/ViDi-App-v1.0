package com.minhhuycoder.vidi.core

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.databinding.ItemReviewBinding
import com.minhhuycoder.vidi.models.ReviewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ReviewAdapter - Bộ nạp dữ liệu cho danh sách đánh giá
 * Sử dụng ListAdapter và DiffUtil để tối ưu hóa việc cập nhật danh sách
 */
class ReviewAdapter : ListAdapter<ReviewModel, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    // Định dạng ngày hiển thị: dd/MM/yyyy
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * ViewHolder sử dụng ViewBinding để truy cập các View trong item_review.xml
     */
    class ReviewViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            // Gán dữ liệu vào các View8
            tvUserName.text = item.username
            tvReviewText.text = item.comment
            
            // Xử lý hiển thị ngày tháng từ Firebase Timestamp
            item.timestamp?.let {
                tvReviewDate.text = dateFormat.format(it.toDate())
            }

            // Hiển thị số sao dựa trên rating (logic xử lý trực tiếp trên LinearLayout)
            updateRatingStars(holder, item.rating)
        }
    }

    /**
     * Cập nhật màu sắc các ngôi sao dựa trên số điểm đánh giá
     */
    private fun updateRatingStars(holder: ReviewViewHolder, rating: Float) {
        val starsContainer = holder.binding.llStars
        val ratingInt = rating.toInt()
        
        for (i in 0 until starsContainer.childCount) {
            val starView = starsContainer.getChildAt(i) as? ImageView
            starView?.let {
                if (i < ratingInt) {
                    it.setImageResource(android.R.drawable.btn_star_big_on)
                } else {
                    it.setImageResource(android.R.drawable.btn_star_big_off)
                }
            }
        }
    }

    /**
     * DiffUtil giúp RecyclerView xác định đúng item nào thay đổi để cập nhật mượt mà
     */
    class ReviewDiffCallback : DiffUtil.ItemCallback<ReviewModel>() {
        override fun areItemsTheSame(oldItem: ReviewModel, newItem: ReviewModel): Boolean {
            return oldItem.reviewId == newItem.reviewId
        }

        override fun areContentsTheSame(oldItem: ReviewModel, newItem: ReviewModel): Boolean {
            return oldItem == newItem
        }
    }
}

package com.minhhuycoder.vidi.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.R

class AdminReviewAdapter(
    private val reviewList: MutableList<AdminReviewModel>,
    private val onHideClick: (AdminReviewModel) -> Unit,
    private val onDeleteClick: (AdminReviewModel) -> Unit
) : RecyclerView.Adapter<AdminReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewUser: TextView = itemView.findViewById(R.id.tvReviewUser)
        val tvReviewRating: TextView = itemView.findViewById(R.id.tvReviewRating)
        val tvReviewStatus: TextView = itemView.findViewById(R.id.tvReviewStatus)
        val tvReviewComment: TextView = itemView.findViewById(R.id.tvReviewComment)
        val btnHideReview: TextView = itemView.findViewById(R.id.btnHideReview)
        val btnDeleteReview: TextView = itemView.findViewById(R.id.btnDeleteReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_review, parent, false)

        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]

        holder.tvReviewUser.text = review.username.ifEmpty { "Người dùng" }
        holder.tvReviewRating.text = "${review.rating} ★"
        holder.tvReviewComment.text = review.comment.ifEmpty { "Không có nội dung" }

        if (review.status == "hidden") {
            holder.tvReviewStatus.text = "Đã ẩn"
            holder.btnHideReview.text = "Hiện"
        } else {
            holder.tvReviewStatus.text = "Đang hiện"
            holder.btnHideReview.text = "Ẩn"
        }

        holder.btnHideReview.setOnClickListener {
            onHideClick(review)
        }

        holder.btnDeleteReview.setOnClickListener {
            onDeleteClick(review)
        }
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}
package com.minhhuycoder.vidi.admin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.R
import com.minhhuycoder.vidi.models.PlaceModel
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class AdminPlaceAdapter(
    private val placeList: MutableList<PlaceModel>,
    private val onEditClick: (PlaceModel) -> Unit,
    private val onDeleteClick: (PlaceModel) -> Unit,
    private val onLoadReviewClick: (PlaceModel) -> Unit,
    private val onHideReviewClick: (AdminReviewModel) -> Unit,
    private val onDeleteReviewClick: (AdminReviewModel) -> Unit
) : RecyclerView.Adapter<AdminPlaceAdapter.PlaceViewHolder>() {

    private val imageCache = ConcurrentHashMap<String, Bitmap>()
    private val expandedPlaceIds = mutableSetOf<String>()
    private val reviewMap = mutableMapOf<String, MutableList<AdminReviewModel>>()

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPlaceImage: ImageView = itemView.findViewById(R.id.ivPlaceImage)
        val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvPlaceType: TextView = itemView.findViewById(R.id.tvPlaceType)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvReviewCount: TextView = itemView.findViewById(R.id.tvReviewCount)
        val btnMore: ImageView = itemView.findViewById(R.id.btnMore)
        val layoutReviewContainer: LinearLayout = itemView.findViewById(R.id.layoutReviewContainer)
        val rvRecentReviews: RecyclerView = itemView.findViewById(R.id.rvRecentReviews)
        val btnMoreReviews: TextView = itemView.findViewById(R.id.btnMoreReviews)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_place, parent, false)

        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        val imageUrl = place.imageUrl.trim()
        val imageKey = place.placeId + "_" + imageUrl
        val isExpanded = expandedPlaceIds.contains(place.placeId)

        holder.tvPlaceName.text = place.name
        holder.tvPlaceType.text = place.category
        holder.tvAddress.text = place.address
        holder.tvRating.text = "${place.rating} ⭐"
        holder.tvReviewCount.text = "(${place.reviewCount} reviews)"

        holder.ivPlaceImage.tag = imageKey
        holder.ivPlaceImage.setImageResource(android.R.drawable.ic_menu_gallery)
        loadImageFromUrl(place.placeId, imageUrl, holder.ivPlaceImage)

        holder.layoutReviewContainer.visibility = if (isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }

        if (isExpanded) {
            val reviews = reviewMap[place.placeId] ?: mutableListOf()

            holder.btnMoreReviews.text = "Ẩn đánh giá (${reviews.size})"

            holder.rvRecentReviews.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.rvRecentReviews.isNestedScrollingEnabled = false
            holder.rvRecentReviews.adapter = AdminReviewAdapter(
                reviews,
                onHideClick = { review ->
                    onHideReviewClick(review)
                },
                onDeleteClick = { review ->
                    onDeleteReviewClick(review)
                }
            )
        } else {
            holder.btnMoreReviews.text = "Xem đánh giá"
        }

        // Bấm vào thẻ địa điểm để mở / đóng review
        holder.itemView.setOnClickListener {
            toggleReview(place, holder.adapterPosition)
        }

        // Bấm dòng Ẩn đánh giá để đóng lại
        holder.btnMoreReviews.setOnClickListener {
            toggleReview(place, holder.adapterPosition)
        }

        // Bấm nút nhỏ bên phải để sửa / xóa địa điểm
        holder.btnMore.setOnClickListener {
            showActionMenu(holder.btnMore, place)
        }
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    fun updateData(newList: List<PlaceModel>) {
        placeList.clear()
        placeList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setReviewsForPlace(placeId: String, reviews: List<AdminReviewModel>) {
        reviewMap[placeId] = reviews.toMutableList()
        notifyDataSetChanged()
    }

    private fun toggleReview(place: PlaceModel, position: Int) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        if (expandedPlaceIds.contains(place.placeId)) {
            expandedPlaceIds.remove(place.placeId)
        } else {
            expandedPlaceIds.add(place.placeId)
            onLoadReviewClick(place)
        }

        notifyItemChanged(position)
    }

    private fun showActionMenu(anchorView: View, place: PlaceModel) {
        val popupMenu = PopupMenu(anchorView.context, anchorView)

        popupMenu.menu.add("Sửa địa điểm")
        popupMenu.menu.add("Xóa địa điểm")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Sửa địa điểm" -> {
                    onEditClick(place)
                    true
                }

                "Xóa địa điểm" -> {
                    onDeleteClick(place)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun loadImageFromUrl(placeId: String, imageUrl: String, imageView: ImageView) {
        val imageKey = placeId + "_" + imageUrl

        if (imageUrl.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        val cachedBitmap = imageCache[imageKey]
        if (cachedBitmap != null) {
            if (imageView.tag == imageKey) {
                imageView.setImageBitmap(cachedBitmap)
            }
            return
        }

        Thread {
            try {
                val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())

                imageCache[imageKey] = bitmap

                imageView.post {
                    if (imageView.tag == imageKey) {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                imageView.post {
                    if (imageView.tag == imageKey) {
                        imageView.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                }
            }
        }.start()
    }
}
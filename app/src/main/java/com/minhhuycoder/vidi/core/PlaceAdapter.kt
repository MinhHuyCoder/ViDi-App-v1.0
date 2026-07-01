package com.minhhuycoder.vidi.core

import android.widget.ImageView
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.minhhuycoder.vidi.models.PlaceModel
import com.minhhuycoder.vidi.R

class PlaceAdapter : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    private val placeList = mutableListOf<PlaceModel>()

    // === THÊM: Lambda biến lưu trữ sự kiện click từ MainActivity chuyển qua ===
    private var onItemClickListener: ((PlaceModel) -> Unit)? = null

    fun setOnItemClickListener(listener: (PlaceModel) -> Unit) {
        onItemClickListener = listener
    }

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvPlaceType: TextView = itemView.findViewById(R.id.tvPlaceType)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val tvOpen: TextView = itemView.findViewById(R.id.tvOpen)
        val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        val ivPlaceImage: ImageView = itemView.findViewById(R.id.ivPlaceImage)
    }

    fun submitList(newList: List<PlaceModel>) {
        placeList.clear()
        placeList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {

        val place = placeList[position]

        holder.tvName.text = place.name

        holder.tvPlaceType.text = place.category

        holder.tvAddress.text = place.address

        // === SỬA: Đổi từ place.rating sang place.ratingAverage theo tài liệu nhóm ===
        holder.tvRating.text = String.format("%.1f", place.ratingAverage)

        holder.tvOpen.text =
            if (place.status) {
                "🟢 Mở đến ${place.closeTime}"
            } else {
                "🔴 Đã đóng"
            }

        holder.ivFavorite.setImageResource(
            android.R.drawable.btn_star_big_off
        )

        Glide.with(holder.itemView.context)
            .load(place.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(holder.ivPlaceImage)

        // === THÊM: Lắng nghe sự kiện click vào item quán để chuyển trang ===
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(place)
        }
    }

    override fun getItemCount() = placeList.size
}
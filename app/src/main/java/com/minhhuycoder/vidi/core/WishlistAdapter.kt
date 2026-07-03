package com.minhhuycoder.vidi.wishlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.minhhuycoder.vidi.databinding.ItemPlaceBinding
import com.minhhuycoder.vidi.models.PlaceModel // Import model chung của nhóm

class WishlistAdapter(
    private var places: List<PlaceModel>,
    private val onHeartClicked: (PlaceModel) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(val binding: ItemPlaceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WishlistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val place = places[position]
        val binding = holder.binding

        // Đổ dữ liệu text vào đúng các ID trong item_place.xml
        binding.tvPlaceName.text = place.name
        binding.tvPlaceType.text = place.category
        binding.tvAddress.text = place.address
        binding.tvRating.text = place.rating.toString()

        // Nạp link ảnh URL trực tiếp mượt mà bằng thư viện Glide
        Glide.with(binding.ivPlaceImage.context)
            .load(place.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivPlaceImage)

        // Vì là màn danh sách yêu thích nên mặc định đặt trạng thái icon đã thả tim
        binding.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on)

        // Bắt sự kiện click nút Tim để xử lý xóa nhanh
        binding.ivFavorite.setOnClickListener {
            onHeartClicked(place)
        }
    }

    override fun getItemCount(): Int = places.size

    // Hàm cập nhật nhanh danh sách khi có thay đổi dữ liệu hoặc tìm kiếm
    fun submitList(newList: List<PlaceModel>) {
        places = newList
        notifyDataSetChanged()
    }
}
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

class PlaceAdapter(private val placeList: List<PlaceModel>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)

        val tvPlaceType = itemView.findViewById<TextView>(R.id.tvPlaceType)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        val ivPlaceImage: ImageView = itemView.findViewById(R.id.ivPlaceImage)
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
        holder.tvRating.text = "${place.ratingAverage} ★"

        Glide.with(holder.itemView.context)
            .load(place.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivPlaceImage)
    }

    override fun getItemCount() = placeList.size
}
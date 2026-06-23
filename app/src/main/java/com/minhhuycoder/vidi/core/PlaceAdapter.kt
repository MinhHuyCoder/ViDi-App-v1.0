package com.minhhuycoder.vidi.core

import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.minhhuycoder.vidi.models.PlaceModel
import com.minhhuycoder.vidi.R

class PlaceAdapter(private val placeList: List<PlaceModel>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Khai báo các view trong item_place.xml ở đây
        val tvName: TextView = itemView.findViewById(R.id.tvName) // Sửa ID theo XML của mày
        val tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.tvName.text = place.name
        holder.tvAddress.text = place.address

        // holder.tvDescription.text = place.description
    }

    override fun getItemCount() = placeList.size
}
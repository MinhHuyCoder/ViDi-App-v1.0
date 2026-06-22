package core

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import models.PlaceModel
import com.minhhuycoder.vidi.R

class PlaceAdapter(private val placeList: List<PlaceModel>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Sau này Kiều vẽ XML xong thì thêm findViewById ở đây
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        // Đổ dữ liệu vào view ở đây
    }

    override fun getItemCount() = placeList.size
}
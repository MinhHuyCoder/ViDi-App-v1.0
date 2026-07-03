package com.minhhuycoder.vidi.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.minhhuycoder.vidi.R

class AdminUserAdapter(
    private val userList: MutableList<AdminUserModel>,
    private val onToggleStatusClick: (AdminUserModel) -> Unit
) : RecyclerView.Adapter<AdminUserAdapter.UserViewHolder>() {

    // Lưu user nào đang mở chi tiết
    private val expandedUserIds = mutableSetOf<String>()

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserInitial: TextView = itemView.findViewById(R.id.tvUserInitial)
        val ivUserAvatar: ImageView = itemView.findViewById(R.id.ivUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val tvUserStatus: TextView = itemView.findViewById(R.id.tvUserStatus)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)
        val btnMore: ImageView = itemView.findViewById(R.id.btnMore)
        val layoutUserDetail: LinearLayout = itemView.findViewById(R.id.layoutUserDetail)
        val tvJoinedDate: TextView = itemView.findViewById(R.id.tvJoinedDate)
        val tvReviewsCount: TextView = itemView.findViewById(R.id.tvReviewsCount)
        val tvSavedPlacesCount: TextView = itemView.findViewById(R.id.tvSavedPlacesCount)
        val btnLockAccount: MaterialButton = itemView.findViewById(R.id.btnLockAccount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_user, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        val isExpanded = expandedUserIds.contains(user.uid)

        val displayName = user.username.ifEmpty { "Chưa có tên" }

        // Hiển thị avatar bằng chữ cái đầu
        holder.ivUserAvatar.visibility = View.GONE
        holder.tvUserInitial.visibility = View.VISIBLE
        holder.tvUserInitial.text = displayName.first().uppercase()

        holder.tvUserName.text = displayName
        holder.tvUserEmail.text = user.email.ifEmpty { "Chưa có email" }
        holder.tvUserRole.text = user.role.ifEmpty { "user" }

        // Hiển thị trạng thái user
        if (user.status == "locked") {
            holder.tvUserStatus.text = "🔴 Đã khóa"
            holder.tvUserStatus.setTextColor(0xFFDC2626.toInt())
            holder.btnLockAccount.text = "Mở khóa tài khoản"
        } else {
            holder.tvUserStatus.text = "🟢 Hoạt động"
            holder.tvUserStatus.setTextColor(0xFF10B981.toInt())
            holder.btnLockAccount.text = "Khóa tài khoản"
        }

        holder.tvJoinedDate.text = "Ngày tham gia: ${user.joinDate}"
        holder.tvReviewsCount.text = "Đã đánh giá: ${user.totalReviews}"
        holder.tvSavedPlacesCount.text = "Đã lưu: ${user.wishlistCount} địa điểm"

        holder.layoutUserDetail.visibility = if (isExpanded) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Bấm vào item để mở / đóng chi tiết
        holder.itemView.setOnClickListener {
            toggleUserDetail(user, holder.adapterPosition)
        }

        // Bấm nút ba chấm để mở menu
        holder.btnMore.setOnClickListener {
            showUserMenu(holder.btnMore, user, holder.adapterPosition)
        }

        // Bấm khóa / mở khóa
        holder.btnLockAccount.setOnClickListener {
            onToggleStatusClick(user)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateData(newList: List<AdminUserModel>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun toggleUserDetail(user: AdminUserModel, position: Int) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        if (expandedUserIds.contains(user.uid)) {
            expandedUserIds.remove(user.uid)
        } else {
            expandedUserIds.add(user.uid)
        }

        notifyItemChanged(position)
    }

    private fun showUserMenu(anchorView: View, user: AdminUserModel, position: Int) {
        val popupMenu = PopupMenu(anchorView.context, anchorView)

        popupMenu.menu.add("Xem chi tiết")

        if (user.status == "locked") {
            popupMenu.menu.add("Mở khóa tài khoản")
        } else {
            popupMenu.menu.add("Khóa tài khoản")
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Xem chi tiết" -> {
                    toggleUserDetail(user, position)
                    true
                }

                "Khóa tài khoản" -> {
                    onToggleStatusClick(user)
                    true
                }

                "Mở khóa tài khoản" -> {
                    onToggleStatusClick(user)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }
}
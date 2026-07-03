package com.minhhuycoder.vidi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.admin.AdminUserAdapter
import com.minhhuycoder.vidi.admin.AdminUserModel
import java.text.SimpleDateFormat
import java.util.Locale

class AdminUserActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var etUserSearch: EditText
    private lateinit var tvTotalUsersCount: TextView
    private lateinit var tvLockedUsersCount: TextView
    private lateinit var tvAdminCount: TextView
    private lateinit var llEmptyState: View
    private lateinit var tabPlaces: View
    private lateinit var tabUsers: View

    private lateinit var userAdapter: AdminUserAdapter

    private val db = FirebaseFirestore.getInstance()

    private val userList = mutableListOf<AdminUserModel>()
    private val allUsers = mutableListOf<AdminUserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user)

        initViews()
        setupRecyclerView()
        setupSearch()
        setupEvents()
        loadUsersFromFirebase()
    }

    private fun initViews() {
        // Ánh xạ đúng ID trong activity_admin_user.xml
        rvUsers = findViewById(R.id.rvUsers)
        etUserSearch = findViewById(R.id.etUserSearch)
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount)
        tvLockedUsersCount = findViewById(R.id.tvLockedUsersCount)
        tvAdminCount = findViewById(R.id.tvAdminCount)
        llEmptyState = findViewById(R.id.llEmptyState)
        tabPlaces = findViewById(R.id.tabPlaces)
        tabUsers = findViewById(R.id.tabUsers)
    }

    private fun setupRecyclerView() {
        userAdapter = AdminUserAdapter(
            userList,
            onToggleStatusClick = { user ->
                showToggleUserDialog(user)
            }
        )

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter
    }

    private fun setupSearch() {
        etUserSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setupEvents() {
        // Bấm tab Địa điểm thì quay lại màn quản lý địa điểm
        tabPlaces.setOnClickListener {
            finish()
        }

        // Đang ở màn người dùng
        tabUsers.setOnClickListener {
            Toast.makeText(this, "Đang ở quản lý người dùng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUsersFromFirebase() {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                allUsers.clear()

                for (document in result) {
                    val uid = document.getString("uid") ?: document.id
                    val username = document.getString("username") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val role = document.getString("role") ?: "user"
                    val status = document.getString("status") ?: "active"

                    val createdAt = document.getTimestamp("createdAt")
                    val joinDate = if (createdAt != null) {
                        formatDate(createdAt.toDate().time)
                    } else {
                        "Chưa có"
                    }

                    val user = AdminUserModel(
                        uid = uid,
                        username = username,
                        email = email,
                        phone = phone,
                        role = role,
                        status = status,
                        joinDate = joinDate,
                        totalReviews = 0,
                        wishlistCount = 0
                    )

                    allUsers.add(user)
                }

                refreshList()
                updateSummary()
                loadExtraUserStats()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi tải user: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadExtraUserStats() {
        // Tính tổng review và wishlist cho từng user
        for (user in allUsers) {
            loadReviewCountForUser(user)
            loadWishlistCountForUser(user)
        }
    }

    private fun loadReviewCountForUser(user: AdminUserModel) {
        db.collection("reviews")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                user.totalReviews = result.size()
                refreshList()
            }
    }

    private fun loadWishlistCountForUser(user: AdminUserModel) {
        db.collection("wishlist")
            .whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener { result ->
                user.wishlistCount = result.size()
                refreshList()
            }
    }

    private fun filterUsers(keyword: String) {
        val searchText = keyword.trim().lowercase()

        if (searchText.isEmpty()) {
            userAdapter.updateData(allUsers)
            updateEmptyState(allUsers)
            return
        }

        val filteredList = allUsers.filter { user ->
            user.username.lowercase().contains(searchText) ||
                    user.email.lowercase().contains(searchText) ||
                    user.phone.lowercase().contains(searchText) ||
                    user.role.lowercase().contains(searchText)
        }

        userAdapter.updateData(filteredList)
        updateEmptyState(filteredList)
    }

    private fun refreshList() {
        val keyword = etUserSearch.text.toString().trim()

        if (keyword.isEmpty()) {
            userAdapter.updateData(allUsers)
            updateEmptyState(allUsers)
        } else {
            filterUsers(keyword)
        }
    }

    private fun updateSummary() {
        tvTotalUsersCount.text = allUsers.size.toString()

        val lockedCount = allUsers.count { user ->
            user.status == "locked"
        }

        val adminCount = allUsers.count { user ->
            user.role.lowercase() == "admin"
        }

        tvLockedUsersCount.text = lockedCount.toString()
        tvAdminCount.text = adminCount.toString()
    }

    private fun updateEmptyState(list: List<AdminUserModel>) {
        if (list.isEmpty()) {
            llEmptyState.visibility = View.VISIBLE
            rvUsers.visibility = View.GONE
        } else {
            llEmptyState.visibility = View.GONE
            rvUsers.visibility = View.VISIBLE
        }
    }

    private fun showToggleUserDialog(user: AdminUserModel) {
        val isLocked = user.status == "locked"

        val title = if (isLocked) {
            "Mở khóa tài khoản"
        } else {
            "Khóa tài khoản"
        }

        val message = if (isLocked) {
            "Bạn có chắc muốn mở khóa tài khoản \"${user.username}\" không?"
        } else {
            "Bạn có chắc muốn khóa tài khoản \"${user.username}\" không?"
        }

        val positiveText = if (isLocked) {
            "Mở khóa"
        } else {
            "Khóa"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ ->
                toggleUserStatus(user)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun toggleUserStatus(user: AdminUserModel) {
        if (user.uid.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy User ID", Toast.LENGTH_SHORT).show()
            return
        }

        val newStatus = if (user.status == "locked") {
            "active"
        } else {
            "locked"
        }

        // Cập nhật trạng thái tài khoản user
        db.collection("users")
            .document(user.uid)
            .update("status", newStatus)
            .addOnSuccessListener {
                user.status = newStatus

                Toast.makeText(this, "Đã cập nhật trạng thái user", Toast.LENGTH_SHORT).show()

                refreshList()
                updateSummary()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Lỗi cập nhật user: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun formatDate(timeMillis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(timeMillis)
    }
}
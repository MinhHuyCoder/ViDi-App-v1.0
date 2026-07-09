package com.minhhuycoder.vidi

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.databinding.ActivityAccountInfoBinding
import com.minhhuycoder.vidi.models.UserModel

class AccountInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountInfoBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadUserInfo()

        binding.btnSave.setOnClickListener {
            saveUserInfo()
        }
    }

    private fun loadUserInfo() {

        val currentUser = auth.currentUser

        if (currentUser == null) {
            finish()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                val user = document.toObject(UserModel::class.java)

                if (user != null) {

                    binding.etUsername.setText(user.username)
                    binding.etEmail.setText(user.email)
                    binding.etPhone.setText(user.phone)

                }

            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Không tải được thông tin",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun saveUserInfo() {

        val currentUser = auth.currentUser ?: return

        val username =
            binding.etUsername.text.toString().trim()

        val phone =
            binding.etPhone.text.toString().trim()

        if (username.isEmpty()) {

            binding.etUsername.error = "Không được để trống"

            return
        }

        if (phone.isNotEmpty() &&
            !phone.matches(Regex("^\\d{10,11}$"))
        ) {

            binding.etPhone.error = "Số điện thoại không hợp lệ"

            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .update(
                mapOf(
                    "username" to username,
                    "phone" to phone
                )
            )
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Đã cập nhật thông tin",
                    Toast.LENGTH_SHORT
                ).show()

                finish()

            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Cập nhật thất bại",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }
}
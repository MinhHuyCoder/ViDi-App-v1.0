package com.minhhuycoder.vidi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.minhhuycoder.vidi.auth.LoginActivity
import com.minhhuycoder.vidi.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {

        val currentPassword =
            binding.etCurrentPassword.text.toString().trim()

        val newPassword =
            binding.etNewPassword.text.toString().trim()

        val confirmPassword =
            binding.etConfirmPassword.text.toString().trim()

        if (currentPassword.isEmpty()) {

            binding.etCurrentPassword.error = "Nhập mật khẩu hiện tại"

            return
        }

        if (newPassword.length < 6) {

            binding.etNewPassword.error = "Mật khẩu tối thiểu 6 ký tự"

            return
        }

        if (newPassword != confirmPassword) {

            binding.etConfirmPassword.error = "Mật khẩu xác nhận không khớp"

            return
        }

        val user = auth.currentUser ?: return

        val credential = EmailAuthProvider.getCredential(
            user.email!!,
            currentPassword
        )

        user.reauthenticate(credential)
            .addOnSuccessListener {

                user.updatePassword(newPassword)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Đổi mật khẩu thành công. Vui lòng đăng nhập lại.",
                            Toast.LENGTH_LONG
                        ).show()

                        auth.signOut()

                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )

                        finishAffinity()

                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Không thể đổi mật khẩu",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

            }
            .addOnFailureListener {

                binding.etCurrentPassword.error =
                    "Mật khẩu hiện tại không đúng"

            }
    }
}
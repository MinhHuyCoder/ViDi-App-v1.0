package com.minhhuycoder.vidi.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minhhuycoder.vidi.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnVerify.setOnClickListener {

            resetPassword()

        }

    }

    private fun resetPassword() {

        val email = binding.etEmailPhone.text.toString().trim()

        if (email.isEmpty()) {

            binding.etEmailPhone.error = "Không được để trống"

            return

        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.etEmailPhone.error = "Email không hợp lệ"

            return

        }

        repository.sendResetPassword(email) { success, message ->

            runOnUiThread {

                if (success) {

                    Toast.makeText(

                        this,

                        "Đã gửi email đặt lại mật khẩu",

                        Toast.LENGTH_LONG

                    ).show()

                    finish()

                } else {

                    Toast.makeText(

                        this,

                        message,

                        Toast.LENGTH_LONG

                    ).show()

                }

            }

        }

    }

}
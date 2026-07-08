package com.minhhuycoder.vidi.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minhhuycoder.vidi.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {

            register()

        }

    }

    private fun register() {

        val email = binding.etEmail.text.toString().trim()

        val phone = binding.etPhone.text.toString().trim()

        val password = binding.etPassword.text.toString()

        val confirm = binding.etConfirmPassword.text.toString()

        if (email.isEmpty()) {

            binding.etEmail.error = "Email không được để trống"

            return

        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.etEmail.error = "Email không hợp lệ"

            return

        }

        if (phone.isEmpty()) {

            binding.etPhone.error = "Không được để trống"

            return

        }

        if (password.length < 6) {

            binding.etPassword.error = "Mật khẩu tối thiểu 6 ký tự"

            return

        }

        if (password != confirm) {

            binding.etConfirmPassword.error = "Mật khẩu không khớp"

            return

        }

        repository.register(

            email,

            password,

            phone

        ) { success, message ->

            runOnUiThread {

                if (success) {

                    Toast.makeText(

                        this,

                        "Đăng ký thành công",

                        Toast.LENGTH_SHORT

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
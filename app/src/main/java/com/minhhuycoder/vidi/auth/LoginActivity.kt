package com.minhhuycoder.vidi.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.minhhuycoder.vidi.MainActivity
import com.minhhuycoder.vidi.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (FirebaseAuth.getInstance().currentUser != null) {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()

            return
        }
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }
    }

    private fun login() {

        val email = binding.etUsername.text.toString().trim()

        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {

            binding.etUsername.error = "Không được để trống"

            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.etUsername.error = "Email không hợp lệ"

            return
        }

        if (password.isEmpty()) {

            binding.etPassword.error = "Không được để trống"

            return
        }
        repository.login(
            email,
            password
        ) { success, message, role, status ->

            runOnUiThread {

                if (!success) {

                    Toast.makeText(
                        this,
                        message ?: "Đăng nhập thất bại",
                        Toast.LENGTH_LONG
                    ).show()

                    return@runOnUiThread
                }

                if (status != "active") {

                    Toast.makeText(
                        this,
                        "Tài khoản đã bị khóa",
                        Toast.LENGTH_LONG
                    ).show()

                    repository.logout()

                    return@runOnUiThread
                }

                when (role) {

                    "admin" -> {

                        startActivity(
                            Intent(this, MainActivity::class.java)
                        )

                    }

                    else -> {

                        startActivity(
                            Intent(this, MainActivity::class.java)
                        )

                    }

                }

                finish()

            }

        }

    }

}
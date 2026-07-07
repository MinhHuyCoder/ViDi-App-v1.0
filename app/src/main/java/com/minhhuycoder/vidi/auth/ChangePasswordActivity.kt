package com.minhhuycoder.vidi.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.minhhuycoder.vidi.databinding.ActivityNewPasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewPasswordBinding

    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewPasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnAccept.setOnClickListener {

            changePassword()

        }

    }

    private fun changePassword() {

        val password =

            binding.etNewPassword.text.toString()

        val confirm =

            binding.etConfirmNewPassword.text.toString()

        if (password.length < 6) {

            binding.etNewPassword.error =

                "Mật khẩu tối thiểu 6 ký tự"

            return

        }

        if (password != confirm) {

            binding.etConfirmNewPassword.error =

                "Mật khẩu không khớp"

            return

        }

        repository.changePassword(password) { success, message ->

            runOnUiThread {

                if (success) {

                    Toast.makeText(

                        this,

                        "Đổi mật khẩu thành công",

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
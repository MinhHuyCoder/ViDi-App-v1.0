package com.minhhuycoder.vidi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DevMenuActivity : AppCompatActivity() {

    private lateinit var btnOpenUser: Button
    private lateinit var btnOpenAdmin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_menu)

        btnOpenUser = findViewById(R.id.btnOpenUser)
        btnOpenAdmin = findViewById(R.id.btnOpenAdmin)

        btnOpenUser.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btnOpenAdmin.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }
    }
}
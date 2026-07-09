package com.minhhuycoder.vidi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.minhhuycoder.vidi.auth.LoginActivity
import com.minhhuycoder.vidi.databinding.ActivityProfileBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.models.UserModel

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(user?.uid ?: return)
            .get()
            .addOnSuccessListener { document ->

                val userModel = document.toObject(UserModel::class.java)

                binding.tvGreeting.text =
                    userModel?.username ?: "Người dùng"

                binding.tvEmail.text =
                    userModel?.email ?: (user.email ?: "Chưa có email")
            }

        db.collection("favorites")
            .whereEqualTo("userId", user?.uid)
            .get()
            .addOnSuccessListener {

                binding.tvFavoriteCount.text =
                    it.size().toString()

            }
        db.collection("reviews")
            .whereEqualTo("userId", user?.uid)
            .get()
            .addOnSuccessListener {

                binding.tvReviewCount.text =
                    it.size().toString()

            }

        binding.btnAccountInfo.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AccountInfoActivity::class.java
                )
            )

        }

        binding.btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finishAffinity()

        }

        binding.btnSecurity.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ChangePasswordActivity::class.java
                )
            )

        }

        binding.bottomNavigation.selectedItemId = R.id.nav_profile

        binding.bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){

                R.id.nav_home -> {

                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true

                }


                R.id.nav_favorite -> {

                    startActivity(Intent(this, WishlistActivity::class.java))
                    finish()
                    true
                }

                R.id.nav_notifications -> {

                    // TODO
                    true
                }

                R.id.nav_profile -> true

                else -> false
            }
        }
    }
}
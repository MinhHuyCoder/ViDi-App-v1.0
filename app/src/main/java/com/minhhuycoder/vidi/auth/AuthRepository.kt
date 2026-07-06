package com.minhhuycoder.vidi.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.minhhuycoder.vidi.models.UserModel

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ================= REGISTER =================

    fun register(
        email: String,
        password: String,
        phone: String,
        callback: (Boolean, String?) -> Unit
    ) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val uid = auth.currentUser!!.uid

                val username = email.substringBefore("@")

                val user = UserModel(
                    uid = uid,
                    email = email,
                    phone = phone,
                    username = username,
                    role = "user",
                    status = "active"
                )

                db.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener {

                        callback(true, null)

                    }
                    .addOnFailureListener {

                        callback(false, it.message)

                    }

            }
            .addOnFailureListener {

                callback(false, it.message)

            }

    }

    // ================= LOGIN =================

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String?, String?, String?) -> Unit
    ) {

        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener {

                val uid = auth.currentUser!!.uid

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->

                        if (!document.exists()) {

                            callback(false, "Không tìm thấy người dùng", null, null)
                            return@addOnSuccessListener

                        }

                        val role = document.getString("role") ?: "user"

                        val status = document.getString("status") ?: "active"

                        callback(true, null, role, status)

                    }

                    .addOnFailureListener {

                        callback(false, it.message, null, null)

                    }

            }

            .addOnFailureListener {

                callback(false, it.message, null, null)

            }

    }

    // ================= RESET PASSWORD =================

    fun sendResetPassword(
        email: String,
        callback: (Boolean, String?) -> Unit
    ) {

        auth.sendPasswordResetEmail(email)

            .addOnSuccessListener {

                callback(true, null)

            }

            .addOnFailureListener {

                callback(false, it.message)

            }

    }

    // ================= CHANGE PASSWORD =================

    fun changePassword(
        newPassword: String,
        callback: (Boolean, String?) -> Unit
    ) {

        auth.currentUser

            ?.updatePassword(newPassword)

            ?.addOnSuccessListener {

                callback(true, null)

            }

            ?.addOnFailureListener {

                callback(false, it.message)

            }

    }

    // ================= LOGOUT =================

    fun logout() {

        auth.signOut()

    }

}
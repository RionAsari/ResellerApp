package com.example.resellerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.resellerapp.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnReset.setOnClickListener {
            val email = binding.edtEmailReset.text.toString().trim()

            if (!validateEmail(email)) return@setOnClickListener

            sendPasswordResetEmail(email)
        }
    }

    // Validasi email
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.edtEmailReset.error = "Email tidak boleh kosong"
            binding.edtEmailReset.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmailReset.error = "Format email tidak valid"
            binding.edtEmailReset.requestFocus()
            return false
        }

        return true
    }

    // Kirim email reset password
    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Email reset password telah dikirim", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Gagal mengirim email reset password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Navigasi ke Login
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

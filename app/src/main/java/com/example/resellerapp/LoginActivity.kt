package com.example.resellerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.resellerapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Handle klik "Forgot Password"
        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }


        // Handle klik tombol Login
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            if (!validateInput(email, password)) return@setOnClickListener

            loginWithFirebase(email, password)
        }
    }

    // Fungsi untuk validasi input email dan password
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.edtEmailLogin.error = "Email harus diisi"
            binding.edtEmailLogin.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmailLogin.error = "Format email tidak valid"
            binding.edtEmailLogin.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.edtPasswordLogin.error = "Password harus diisi"
            binding.edtPasswordLogin.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.edtPasswordLogin.error = "Password minimal 6 karakter"
            binding.edtPasswordLogin.requestFocus()
            return false
        }

        return true
    }

    // Fungsi login ke Firebase
    private fun loginWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Selamat datang $email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, task.exception?.localizedMessage ?: "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

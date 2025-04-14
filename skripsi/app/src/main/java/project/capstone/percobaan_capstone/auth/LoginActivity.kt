package project.capstone.percobaan_capstone.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import project.capstone.percobaan_capstone.MainActivity
import project.capstone.percobaan_capstone.R
import project.capstone.percobaan_capstone.supabase.SupabaseAuthHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var authHelper: SupabaseAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authHelper = SupabaseAuthHelper(this) // ✅ Perbaikan: Harus meneruskan `this` sebagai Context

        // 🔥 Cek apakah user sudah login sebelumnya
        if (authHelper.isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // 🔥 Validasi input
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Email dan password tidak boleh kosong!")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Format email tidak valid!")
                return@setOnClickListener
            }

            // 🔥 Proses login dengan Supabase
            authHelper.login(email, password) { success, message ->
                runOnUiThread {
                    if (success) {
                        showToast("Login Berhasil!")
                        navigateToMainActivity()
                    } else {
                        showToast("Login Gagal: $message")
                    }
                }
            }
        }

        // 🔥 Jika belum punya akun, pindah ke RegisterActivity
        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🔥 Pindah ke MainActivity setelah login berhasil
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // ✅ Perbaikan: Tutup LoginActivity agar tidak bisa dikembalikan dengan tombol back
    }

    // 🔥 Helper function untuk menampilkan Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

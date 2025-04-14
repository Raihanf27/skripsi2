package project.capstone.percobaan_capstone.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import project.capstone.percobaan_capstone.MainActivity
import project.capstone.percobaan_capstone.R
import project.capstone.percobaan_capstone.supabase.SupabaseAuthHelper

class RegisterActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var authHelper: SupabaseAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authHelper = SupabaseAuthHelper(this) // ✅ Perbaikan: Berikan 'this' sebagai context

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Email dan password tidak boleh kosong!")
                return@setOnClickListener
            }

            if (password.length < 6) {
                showToast("Password harus memiliki setidaknya 6 karakter!")
                return@setOnClickListener
            }

            authHelper.register(email, password) { success, message ->
                runOnUiThread {
                    if (success) {
                        showToast("Registrasi berhasil! Silakan login.")
                        navigateToMainActivity()
                    } else {
                        showToast("Registrasi gagal: $message")
                    }
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

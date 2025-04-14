package project.capstone.percobaan_capstone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import project.capstone.percobaan_capstone.auth.LoginActivity
import project.capstone.percobaan_capstone.detection.FaceDetectActivity
import project.capstone.percobaan_capstone.history.HistoryActivity
import project.capstone.percobaan_capstone.supabase.SupabaseAuthHelper

class MainActivity : AppCompatActivity() {
    private lateinit var authHelper: SupabaseAuthHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authHelper = SupabaseAuthHelper(this)

        // Inisialisasi tombol
        val btnDetect = findViewById<Button>(R.id.btnDetect)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Intent ke FaceDetectActivity
        btnDetect.setOnClickListener {
            val intent = Intent(this, FaceDetectActivity::class.java)
            startActivity(intent)
        }

        // Intent ke HistoryActivity
        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Tombol Logout
        btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah kamu yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                authHelper.logout()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
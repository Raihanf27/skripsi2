package project.capstone.percobaan_capstone.history

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import project.capstone.percobaan_capstone.R
import project.capstone.percobaan_capstone.supabase.SupabaseAuthHelper
import project.capstone.percobaan_capstone.supabase.SupabaseHelper

class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var authHelper: SupabaseAuthHelper
    private lateinit var supabaseHelper: SupabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // ✅ Pastikan RecyclerView memiliki ID yang benar
        recyclerView = findViewById(R.id.recyclerViewHistory)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(emptyList())
        recyclerView.adapter = adapter

        authHelper = SupabaseAuthHelper(this)
        supabaseHelper = SupabaseHelper(authHelper)

        fetchHistoryData()
    }

    private fun fetchHistoryData() {
        authHelper.getCurrentUserId { userId ->
            if (userId.isNullOrEmpty()) {
                Log.e("HistoryActivity", "❌ User tidak ditemukan, pindah ke LoginActivity")
                authHelper.forceLogout()
            } else {
                loadUserHistory(userId)
            }
        }
    }

    private fun loadUserHistory(userId: String) {
        supabaseHelper.getHistoryData { historyList ->
            runOnUiThread {
                Log.d("HistoryActivity", "📌 Data diperbarui dari Supabase: ${historyList.size} item")
                adapter.updateData(historyList)
            }
        }
    }
}

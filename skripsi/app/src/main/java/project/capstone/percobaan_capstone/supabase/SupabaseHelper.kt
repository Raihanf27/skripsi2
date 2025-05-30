package project.capstone.percobaan_capstone.supabase

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import project.capstone.percobaan_capstone.history.HistoryItem
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SupabaseHelper(private val authHelper: SupabaseAuthHelper) {

    private val supabaseUrl = ""
    private val supabaseKey = ""

    private val client = OkHttpClient()

    /**
     * 🔥 Simpan data prediksi ke Supabase
     */
    fun savePrediction(imageUrl: String, predictedWeight: Float, weightCategory: String) {
        val userId = authHelper.getSavedUserId()
        val accessToken = authHelper.getAccessToken()

        if (userId.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            Log.e("Supabase", "❌ Gagal menyimpan data: User belum login atau token kosong")
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = dateFormat.format(Date())

        val json = JSONObject().apply {
            put("user_id", userId)
            put("image_url", imageUrl)
            put("predicted_weight", predictedWeight.toDouble())
            put("weight_category", weightCategory)
            put("timestamp", timestamp)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$supabaseUrl/rest/v1/history")
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", supabaseKey) // 🔥 Tambahkan API key ke request
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Supabase", "❌ Gagal menyimpan data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Log.e("Supabase", "❌ Error menyimpan data: ${response.message} - ${responseBody ?: "No Response"}")
                        return
                    }
                    Log.d("Supabase", "✅ Data berhasil disimpan ke Supabase!")
                }
            }
        })
    }

    /**
     * 🔥 Ambil data history hanya milik user yang login
     */
    fun getHistoryData(callback: (List<HistoryItem>) -> Unit) {
        val userId = authHelper.getSavedUserId()
        val accessToken = authHelper.getAccessToken()

        if (userId.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            Log.e("Supabase", "❌ Gagal mengambil data: User belum login atau token kosong")
            callback(emptyList())
            return
        }

        val request = Request.Builder()
            .url("$supabaseUrl/rest/v1/history?select=*&user_id=eq.$userId")
            .get()
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("apikey", supabaseKey) // 🔥 Tambahkan API key ke request
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Supabase", "❌ Gagal mengambil data: ${e.message}")
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val historyList = mutableListOf<HistoryItem>()
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        Log.e("Supabase", "❌ Error mengambil data: ${response.message} - ${responseBody ?: "No Response"}")
                        callback(emptyList())
                        return
                    }

                    val jsonArray = JSONArray(responseBody)
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val item = HistoryItem(
                            json.getString("image_url"),
                            json.getDouble("predicted_weight").toFloat(),
                            json.getString("weight_category"),
                            json.optString("timestamp", "Tidak Ada Waktu")
                        )
                        historyList.add(item)
                    }
                }
                callback(historyList)
            }
        })
    }
}

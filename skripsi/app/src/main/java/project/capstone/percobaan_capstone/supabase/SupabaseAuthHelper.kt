package project.capstone.percobaan_capstone.supabase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import project.capstone.percobaan_capstone.auth.LoginActivity
import android.app.AlertDialog

class SupabaseAuthHelper(private val context: Context) {

    private val supabaseUrl = ""
    private val supabaseKey = ""
    private val client = OkHttpClient()
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains("access_token")
    }

    fun getSavedUserId(): String? {
        return sharedPreferences.getString("user_id", null)
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$supabaseUrl/auth/v1/signup")
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    login(email, password, callback)
                } else {
                    callback(false, responseBody)
                }
            }
        })
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$supabaseUrl/auth/v1/token?grant_type=password")
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val accessToken = jsonResponse.optString("access_token", "")
                    val refreshToken = jsonResponse.optString("refresh_token", "")

                    if (accessToken.isNotEmpty() && refreshToken.isNotEmpty()) {
                        sharedPreferences.edit()
                            .putString("access_token", accessToken)
                            .putString("refresh_token", refreshToken)
                            .apply()
                        getCurrentUserId { userId -> callback(userId != null, "Login berhasil") }
                    } else {
                        callback(false, "Token tidak ditemukan dalam respons")
                    }
                } else {
                    callback(false, responseBody)
                }
            }
        })
    }

    fun getCurrentUserId(callback: (String?) -> Unit) {
        val accessToken = getAccessToken()
        if (accessToken.isNullOrEmpty()) {
            forceLogout()
            callback(null)
            return
        }

        val request = Request.Builder()
            .url("$supabaseUrl/auth/v1/user")
            .get()
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    val errorJson = JSONObject(responseBody ?: "{}")
                    val errorCode = errorJson.optString("error_code", "")
                    if (errorCode == "bad_jwt") {
                        refreshToken { success ->
                            if (success) {
                                getCurrentUserId(callback)
                            } else {
                                forceLogout()
                                callback(null)
                            }
                        }
                    } else {
                        callback(null)
                    }
                    return
                }

                val jsonResponse = JSONObject(responseBody)
                val userId = jsonResponse.optString("id", null)
                userId?.let { sharedPreferences.edit().putString("user_id", it).apply() }
                callback(userId)
            }
        })
    }

    fun refreshToken(callback: (Boolean) -> Unit) {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        if (refreshToken.isNullOrEmpty()) {
            callback(false)
            return
        }

        val json = JSONObject().apply { put("refresh_token", refreshToken) }
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$supabaseUrl/auth/v1/token?grant_type=refresh_token")
            .post(requestBody)
            .addHeader("apikey", supabaseKey)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    callback(false)
                    return
                }
                val jsonResponse = JSONObject(responseBody)
                val newAccessToken = jsonResponse.optString("access_token", "")
                if (newAccessToken.isNotEmpty()) {
                    sharedPreferences.edit().putString("access_token", newAccessToken).apply()
                    callback(true)
                } else {
                    callback(false)
                }
            }
        })
    }

    fun forceLogout() {
        val activity = context as? android.app.Activity ?: return

        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle("Sesi Habis")
                .setMessage("Sesi kamu habis. Silakan login kembali.")
                .setCancelable(false) // Supaya pengguna tidak bisa dismiss pop-up
                .setPositiveButton("Login") { _, _ ->
                    sharedPreferences.edit().clear().apply()
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
                .show()
        }
    }
    fun logout() {
        sharedPreferences.edit().clear().apply() // Hapus semua sesi pengguna

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

}

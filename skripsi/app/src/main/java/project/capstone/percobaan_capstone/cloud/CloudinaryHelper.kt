package project.capstone.percobaan_capstone.cloud

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class CloudinaryHelper {
    private val cloudName = ""
    private val uploadPreset = "your_upload_preset"

    fun uploadImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "data:image/jpeg;base64," + Base64.encodeToString(data, Base64.DEFAULT))
            .addFormDataPart("upload_preset", uploadPreset)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Cloudinary", "❌ Gagal upload: ${e.message}")
                onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {  // Gunakan .use agar response body otomatis ditutup
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        onFailure(IOException("Gagal upload: ${response.message}"))
                        return
                    }

                    val json = JSONObject(responseBody)
                    val imageUrl = json.getString("secure_url")
                    Log.d("Cloudinary", "✅ Gambar berhasil diunggah: $imageUrl")
                    onSuccess(imageUrl)
                }
            }
        })
    }
}

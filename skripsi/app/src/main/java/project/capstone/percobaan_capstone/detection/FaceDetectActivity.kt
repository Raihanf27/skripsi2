package project.capstone.percobaan_capstone.detection

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import project.capstone.percobaan_capstone.R
import project.capstone.percobaan_capstone.clasifier.CattleWeightClassifier
import project.capstone.percobaan_capstone.cloud.CloudinaryHelper
import project.capstone.percobaan_capstone.supabase.SupabaseHelper
import project.capstone.percobaan_capstone.supabase.SupabaseAuthHelper

class FaceDetectActivity : AppCompatActivity() {

    private lateinit var classifier: CattleWeightClassifier
    private lateinit var imageViewOriginal: ImageView
    private lateinit var imageViewSegmented: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var weightCategoryTextView: TextView
    private lateinit var buttonCamera: Button
    private lateinit var buttonGallery: Button
    private lateinit var authHelper: SupabaseAuthHelper
    private lateinit var supabaseHelper: SupabaseHelper
    private lateinit var cloudinaryHelper: CloudinaryHelper

    private val REQUEST_CAMERA = 1
    private val REQUEST_GALLERY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detect)

        // Inisialisasi helper
        authHelper = SupabaseAuthHelper(this)
        supabaseHelper = SupabaseHelper(authHelper)
        cloudinaryHelper = CloudinaryHelper()

        // Inisialisasi UI
        imageViewOriginal = findViewById(R.id.imageViewOriginal)
        imageViewSegmented = findViewById(R.id.imageViewSegmented)
        resultTextView = findViewById(R.id.result)
        weightCategoryTextView = findViewById(R.id.weightCategory)
        buttonCamera = findViewById(R.id.buttonCamera)
        buttonGallery = findViewById(R.id.buttonGallery)

        classifier = CattleWeightClassifier(this)

        buttonCamera.setOnClickListener {
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        }

        buttonGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_GALLERY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val bitmap: Bitmap? = when (requestCode) {
                REQUEST_CAMERA -> data?.extras?.get("data") as? Bitmap
                REQUEST_GALLERY -> data?.data?.let { uri ->
                    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                else -> null
            }

            bitmap?.let { originalBitmap ->
                // 1️⃣ **Tampilkan gambar asli di ImageView**
                imageViewOriginal.setImageBitmap(originalBitmap)

                // 2️⃣ **Jalankan pipeline klasifikasi → segmentasi → prediksi bobot**
                if (classifier.isCattle(originalBitmap)) {
                    val segmentedBitmap = classifier.segmentImage(originalBitmap)
                    val predictedWeight = classifier.predictWeight(segmentedBitmap) + 40

                    // 3️⃣ **Tampilkan gambar hasil segmentasi di ImageView**
                    imageViewSegmented.setImageBitmap(segmentedBitmap)

                    val weightCategory = categorizeWeight(predictedWeight)

                    runOnUiThread {
                        resultTextView.text = "Bobot: $predictedWeight kg"
                        weightCategoryTextView.text = "Kategori: $weightCategory"
                    }

                    // 🔥 Simpan **gambar asli** ke Cloudinary & Supabase
                    saveData(originalBitmap, predictedWeight, weightCategory)
                } else {
                    runOnUiThread {
                        resultTextView.text = "Gambar bukan sapi, tidak bisa diprediksi"
                        weightCategoryTextView.text = ""
                    }
                }
            } ?: run {
                runOnUiThread {
                    resultTextView.text = "Error: Gagal memproses gambar"
                    weightCategoryTextView.text = ""
                }
            }
        }
    }

    private fun categorizeWeight(weight: Float): String {
        return when {
            weight < 300 -> "Kecil"
            weight in 300f..599f -> "Sedang"
            weight > 600 -> "Besar"
            else -> "Tidak diketahui"
        }
    }

    /**
     * 🔥 Simpan **gambar asli** ke Cloudinary dan data ke Supabase
     */
    private fun saveData(originalBitmap: Bitmap, predictedWeight: Float, weightCategory: String) {
        cloudinaryHelper.uploadImage(originalBitmap,
            onSuccess = { imageUrl ->
                Log.d("FaceDetectActivity", "✅ Gambar berhasil diunggah ke Cloudinary: $imageUrl")

                // 🔥 Simpan data ke Supabase setelah gambar berhasil diunggah
                supabaseHelper.savePrediction(imageUrl, predictedWeight, weightCategory)
            },
            onFailure = { exception ->
                Log.e("FaceDetectActivity", "❌ Gagal mengunggah gambar: ${exception.message}")
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
package project.capstone.percobaan_capstone.clasifier

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CattleWeightClassifier(context: Context) {
    private var weightModel: Interpreter
    private var classificationModel: Interpreter
    private var segmentationModel: Interpreter

    init {
        try {
            val weightModelFile = FileUtil.loadMappedFile(context, "mobilenetv2bestmodel.tflite")
            val classificationModelFile = FileUtil.loadMappedFile(context, "klasifikasisapi2.tflite")
            val segmentationModelFile = FileUtil.loadMappedFile(context, "segmentasi_model_unet_best_224.tflite")

            val options = Interpreter.Options().apply { setNumThreads(4) }
            weightModel = Interpreter(weightModelFile, options)
            classificationModel = Interpreter(classificationModelFile, options)
            segmentationModel = Interpreter(segmentationModelFile, options)
        } catch (e: Exception) {
            throw RuntimeException("Error loading models: ${e.message}")
        }
    }

    fun isCattle(bitmap: Bitmap): Boolean {
        val input = preprocessImage(bitmap, 224, 224, 3)
        val output = Array(1) { FloatArray(1) }
        classificationModel.run(input, output)
        Log.d("Classification Output", output.contentDeepToString())
        return output[0][0] > 0.5f
    }

    fun segmentImage(bitmap: Bitmap): Bitmap {
        val input = preprocessImage(bitmap, 224, 224, 3)
        val output = Array(1) { Array(224) { Array(224) { FloatArray(1) } } }
        segmentationModel.run(input, output)

        val segmentedBitmap = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val value = (output[0][y][x][0] * 255).toInt()
                val color = 0xFF shl 24 or (value shl 16) or (value shl 8) or value
                segmentedBitmap.setPixel(x, y, color)
            }
        }

        // Kembalikan hasil segmentasi ke ukuran asli gambar
        return Bitmap.createScaledBitmap(segmentedBitmap, bitmap.width, bitmap.height, true)
    }

    fun predictWeight(bitmap: Bitmap): Float {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = preprocessImage(resizedBitmap, 224, 224, 3)
        val output = Array(1) { FloatArray(1) }
        weightModel.run(input, output)
        Log.d("Weight Output", output.contentDeepToString())

        // Penyesuaian skala jika hasil terlalu rendah
        return output[0][0] * 1.5f
    }

    private fun preprocessImage(bitmap: Bitmap, targetWidth: Int, targetHeight: Int, channels: Int): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * channels)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until targetHeight) {
            for (x in 0 until targetWidth) {
                val pixel = resizedBitmap.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF) / 255.0f
                val g = (pixel shr 8 and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f

                inputBuffer.putFloat(r)
                if (channels > 1) inputBuffer.putFloat(g)
                if (channels > 1) inputBuffer.putFloat(b)
            }
        }
        return inputBuffer
    }

    fun close() {
        weightModel.close()
        classificationModel.close()
        segmentationModel.close()
    }
}
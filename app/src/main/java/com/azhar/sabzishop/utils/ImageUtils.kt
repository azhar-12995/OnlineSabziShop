package com.azhar.sabzishop.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream

/**
 * Utility object for Base64 image conversions.
 * Firestore stores images as compressed Base64 strings inside documents.
 */
object ImageUtils {

    /**
     * Convert a Uri (from gallery picker) to a Base64 encoded string.
     * Compresses and resizes the image before encoding to keep Firestore doc size small.
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmapToBase64(originalBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Compress and convert a Bitmap to Base64 string.
     * Resizes to max dimension to keep size manageable for Firestore (1 MB doc limit).
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val resized = resizeBitmap(bitmap, Constants.IMAGE_MAX_DIMENSION)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESSION_QUALITY, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Decode a Base64 string back to a Bitmap.
     */
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Decode a Base64 string to a Compose-compatible ImageBitmap.
     */
    fun base64ToImageBitmap(base64: String): ImageBitmap? {
        return base64ToBitmap(base64)?.asImageBitmap()
    }

    /**
     * Resize bitmap while maintaining aspect ratio.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }
}

/** Format price with Rs (PKR) symbol */
fun Double.toRupees(): String = "Rs ${String.format("%.0f", this)}"

/** Format quantity as readable string (e.g. 500g, 1 kg, 2.5 kg) */
fun Double.formatQty(): String {
    return if (this < 1.0) {
        "${(this * 1000).toInt()}g"
    } else if (this == this.toInt().toDouble()) {
        "${this.toInt()} kg"
    } else {
        "${String.format("%.1f", this)} kg"
    }
}


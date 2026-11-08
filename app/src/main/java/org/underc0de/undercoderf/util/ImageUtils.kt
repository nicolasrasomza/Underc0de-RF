package org.underc0de.undercoderf.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    /**
     * Carga la imagen elegida (respeta la rotación EXIF) y la deja en 320x240,
     * recortando al centro para mantener la proporción 4:3 sin deformar.
     */
    fun loadForSstv(context: Context, uri: Uri): Bitmap? {
        return try {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= 640 && bounds.outHeight / (sample * 2) >= 480) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val raw = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                ?: return null

            val rotation = try {
                resolver.openInputStream(uri)?.use { readRotation(it) } ?: 0
            } catch (_: Exception) {
                0
            }
            val src = if (rotation != 0) {
                val m = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true)
            } else raw

            // recorte centrado 4:3
            val cropW: Int
            val cropH: Int
            if (src.width * 3 > src.height * 4) {
                cropH = src.height
                cropW = src.height * 4 / 3
            } else {
                cropW = src.width
                cropH = src.width * 3 / 4
            }
            val left = (src.width - cropW) / 2
            val top = (src.height - cropH) / 2

            val out = Bitmap.createBitmap(320, 240, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawColor(0xFF000000.toInt())
            canvas.drawBitmap(
                src,
                Rect(left, top, left + cropW, top + cropH),
                Rect(0, 0, 320, 240),
                Paint(Paint.FILTER_BITMAP_FLAG)
            )
            out
        } catch (_: Throwable) {
            null
        }
    }

    private fun readRotation(stream: java.io.InputStream): Int {
        val exif = ExifInterface(stream)
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    /** Guarda en Pictures/UndercodeRF. Devuelve true si salió bien. */
    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        return try {
            val name = "SSTV_" + java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
                .format(java.util.Date()) + ".png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UndercodeRF")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
                resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    ?: return false
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "UndercodeRF"
                )
                if (!dir.exists() && !dir.mkdirs()) return false
                val file = File(dir, name)
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                android.media.MediaScannerConnection.scanFile(
                    context, arrayOf(file.absolutePath), arrayOf("image/png"), null
                )
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}

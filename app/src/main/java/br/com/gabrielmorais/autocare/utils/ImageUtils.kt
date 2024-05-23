package br.com.gabrielmorais.autocare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class ImageUtils(private val context: Context) {
  fun saveImage(userId: String, uri: Uri): String {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source)

    val file = createImageFile(userId)
    Timber.tag("ImageUtils").i("Caminho do arquivo: ${file.absolutePath}")
    val outputStream = FileOutputStream(file, false)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

    return file.absolutePath
  }

  private fun createImageFile(userFolder: String): File {
    val rootDir = context.getExternalFilesDir("")
    val folder = "$rootDir/$userFolder"
    val fileFolder = File(folder)
    if (!fileFolder.exists()) fileFolder.mkdir()
    val timestamp = System.currentTimeMillis()
    val extension = ".JPG"
    val filename = "$timestamp$extension"
    return File(fileFolder, filename)
  }
}
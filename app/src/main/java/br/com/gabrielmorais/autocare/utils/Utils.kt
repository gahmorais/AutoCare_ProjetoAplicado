package br.com.gabrielmorais.autocare.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class Utils {
  companion object {
    fun imageToBytes(bitmap: Bitmap): ByteArray {
      val baos = ByteArrayOutputStream()
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
      return baos.toByteArray()
    }
    fun diffBetweenMaintenance(currentDate: LocalDate, futureDate: LocalDate): Period {
      return Period.between(currentDate, futureDate)
    }

    fun formatDate(date: LocalDate): String {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      return date.format(dateTimeFormatter)
    }
  }
}
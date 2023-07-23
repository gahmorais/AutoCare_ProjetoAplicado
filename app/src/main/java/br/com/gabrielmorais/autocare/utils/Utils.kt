package br.com.gabrielmorais.autocare.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class Utils {
  companion object {

    fun formatDate(date: Long): String {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      val currentDate = LocalDate.ofEpochDay(date)
      return currentDate.format(dateTimeFormatter)
    }

    fun getLocalDateTime(time: Long): LocalDateTime {
      return LocalDate.ofEpochDay(time).atTime(8, 0)
    }

    fun dateMinusFiveDays(date: LocalDate): LocalDateTime {
      return date.minusDays(5).atTime(7, 0)
    }

    fun calculateNextMaintenanceInMonths(
      averageDistancePerMonth: Int,
      distanceNextMaintenance: Int
    ): Int {
      return distanceNextMaintenance / averageDistancePerMonth
    }

    fun futureDateMonth(currentDate: LocalDate, monthToSum: Int): LocalDate? {
      return currentDate.plusMonths(monthToSum.toLong())
    }

    fun getTime(dateTime: LocalDateTime): Long {
      val minute = dateTime.minute
      val hour = dateTime.hour
      val day = dateTime.dayOfMonth
      val month = dateTime.month.value
      val year = dateTime.year

      val calendar = Calendar.getInstance()
      calendar.set(year, month, day, hour, minute)
      return calendar.timeInMillis
    }
  }
}
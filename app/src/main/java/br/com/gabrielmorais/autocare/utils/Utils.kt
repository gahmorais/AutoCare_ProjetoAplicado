package br.com.gabrielmorais.autocare.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utils {
  companion object {

    fun formatDate(date: Long): String {
      val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
      val currentDate = LocalDate.ofEpochDay(date)
      return currentDate.format(dateTimeFormatter)
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
  }
}
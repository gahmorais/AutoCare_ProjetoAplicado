package br.com.gabrielmorais.autocare.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
  companion object {

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun formatDate(date: Long): String = LocalDate.ofEpochDay(date).format(DATE_FORMATTER)

    fun getLocalDateTime(time: Long): LocalDateTime = LocalDate.ofEpochDay(time).atTime(8, 0)

    /** Horario do lembrete: cinco dias antes da manutencao prevista, as 07h00. */
    fun dateMinusFiveDays(date: LocalDate): LocalDateTime = date.minusDays(5).atTime(7, 0)

    /**
     * Quantos meses ate a proxima manutencao, dada a media mensal percorrida.
     * Retorna null quando a media e invalida - antes o `/` estourava
     * ArithmeticException com media zero.
     */
    fun calculateNextMaintenanceInMonths(
      averageDistancePerMonth: Int,
      distanceNextMaintenance: Int
    ): Int? = if (averageDistancePerMonth > 0) {
      distanceNextMaintenance / averageDistancePerMonth
    } else {
      null
    }

    fun futureDateMonth(currentDate: LocalDate, monthToSum: Int): LocalDate? {
      return currentDate.plusMonths(monthToSum.toLong())
    }
  }
}

package br.com.gabrielmorais.autocare.utils

import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

internal class UtilsTest {
  @Test
  fun `should return current month plus quantity months passed`() {
    val currentDate = LocalDate.of(2023, 7, 9)
    val nextMonth = Utils.futureDateMonth(currentDate, 7)
    Assert.assertEquals(
      nextMonth,
      LocalDate.of(2024, 2, 9)
    )
  }

  @Test
  fun `should return date formatted`() {
    val currentDate = LocalDate.of(2023, 7, 9)
    val dateFormatted = Utils.formatDate(currentDate.toEpochDay())
    Assert.assertEquals(dateFormatted, "09/07/2023")
  }

  @Test
  fun `should return next maintenance date`() {
    val averageDistanceTraveled = 500
    val distanceTraveled = 60000
    val result = Utils.calculateNextMaintenanceInMonths(averageDistanceTraveled, distanceTraveled)
    Assert.assertEquals(result, 120)
  }
}
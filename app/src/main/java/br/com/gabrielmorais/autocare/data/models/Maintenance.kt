package br.com.gabrielmorais.autocare.data.models

import java.time.LocalDate

data class Maintenance(
  val description: String? = null,
  val date: LocalDate? = null,
  val currentMileage: Int? = null,
  val forecastNextExchangeMileage: Int? = null,
  val forecastNextExchangeDate: LocalDate? = null,
  val comments: String? = null
)
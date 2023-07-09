package br.com.gabrielmorais.autocare.data.models

data class Maintenance(
  val description: String? = null,
  val date: Long? = null,
  val currentMileage: Int? = null,
  val forecastNextExchangeMileage: Int? = null,
  val forecastNextExchangeDate: Long? = null,
  val comments: String? = null
)
package br.com.gabrielmorais.autocare.data.models

import java.time.LocalDateTime

data class Maintenance(
  val description: String,
  val date: LocalDateTime,
  val mileage: String
)
package br.com.gabrielmorais.autocare.data.models

import java.util.UUID

data class User(
  val id: String = UUID.randomUUID().toString(),
  val email: String,
  val name: String = "",
  val vehicles: List<Vehicle> = listOf()
)

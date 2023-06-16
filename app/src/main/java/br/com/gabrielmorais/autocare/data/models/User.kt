package br.com.gabrielmorais.autocare.data.models

data class User(
  val id: String,
  val email: String,
  val name: String = "",
  val vehicles: List<Vehicle> = listOf()
)

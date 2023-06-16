package br.com.gabrielmorais.autocare.data.models

data class Vehicle(
  val brand: String,
  val model: String,
  val plate: String,
  val maintenanceRecord: List<Maintenance>
)
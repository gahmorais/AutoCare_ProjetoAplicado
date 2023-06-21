package br.com.gabrielmorais.autocare.data.models

import java.util.UUID

data class Vehicle(
  val id: String = UUID.randomUUID().toString(),
  val brand: String,
  val model: String,
  val plate: String,
  val photo: String? = null,
  val maintenanceRecord: List<Maintenance> = listOf()
)
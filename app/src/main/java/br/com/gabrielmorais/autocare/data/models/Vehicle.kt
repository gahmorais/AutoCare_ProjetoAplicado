package br.com.gabrielmorais.autocare.data.models

import java.util.UUID

data class Vehicle(
  val id: String? = UUID.randomUUID().toString(),
  val nickName: String? = null,
  val brand: String? = null,
  val model: String? = null,
  val plate: String? = null,
  val photo: String? = null,
  val maintenanceRecord: List<Maintenance>? = null
)
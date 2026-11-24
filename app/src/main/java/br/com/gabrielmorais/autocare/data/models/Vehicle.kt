package br.com.gabrielmorais.autocare.data.models

import java.util.UUID

data class Vehicle(
  val id: String? = UUID.randomUUID().toString(),
  val nickName: String? = null,
  val brand: String? = null,
  val model: String? = null,
  val plate: String? = null,
  val photo: String? = null,
  val averageDistanceTraveledPerMonth: Int? = null,
  val maintenances: List<Maintenance>? = null,
  // Dono do veiculo no armazenamento local. Opcional so enquanto o caminho do
  // Firebase ainda existe (ate a etapa 5 do plano); o Room exige o valor.
  val profileId: String? = null
)


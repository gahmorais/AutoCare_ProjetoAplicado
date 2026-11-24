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
  // Perfil dono do veiculo. Opcional no modelo porque a tela monta o veiculo
  // antes de saber o dono - quem grava (MainViewModel.saveVehicle) preenche com o
  // perfil ativo, e o Room recusa veiculo sem dono.
  val profileId: String? = null
)


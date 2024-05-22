package br.com.gabrielmorais.autocare.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class UserVehicles(
  @Embedded val user: User,
  @Relation(
    parentColumn = "id",
    entityColumn = "id"
  )
  val vehicles: List<Vehicle>
)

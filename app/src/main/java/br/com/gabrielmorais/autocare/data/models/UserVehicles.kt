package br.com.gabrielmorais.autocare.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class UserVehicles(
  @Embedded val user: User,
  @Relation(
    parentColumn = "user_id",
    entityColumn = "vehicle_id"
  )
  val vehicles: List<Vehicle>
)

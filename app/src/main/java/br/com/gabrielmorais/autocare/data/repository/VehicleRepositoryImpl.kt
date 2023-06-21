package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import com.google.firebase.database.FirebaseDatabase

class VehicleRepositoryImpl(private val database: FirebaseDatabase) : VehicleRepository {

  override fun save(vehicle: Vehicle, userId: String) {
    database.reference
      .child(userId)
      .child("veiculos")
      .child(vehicle.id)
      .setValue(vehicle)
  }

  override fun update(vehicle: Vehicle, id: String) {
    TODO("Not yet implemented")
  }

  override fun delete(id: String) {
    TODO("Not yet implemented")
  }

  override fun addMaintenance(maintenance: Maintenance) {
    TODO("Not yet implemented")
  }
}
package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle

interface VehicleRepository {
  fun save(vehicle: Vehicle, userId: String)
  fun update(vehicle: Vehicle, id: String)
  fun delete(id: String)
  fun addMaintenance(maintenance: Maintenance)
}
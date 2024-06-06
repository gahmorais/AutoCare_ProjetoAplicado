package br.com.gabrielmorais.autocare.data.repositories.vehicleRepository

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

interface IVehicleRepository {
  suspend fun create(vehicle: Vehicle)
  suspend fun update(vehicle: Vehicle): Int
  suspend fun getById(vehicleId: String): Vehicle
  suspend fun delete(vehicle: Vehicle)
  fun getMaintenances(vehicleId: String): Flow<List<Maintenance>>
}
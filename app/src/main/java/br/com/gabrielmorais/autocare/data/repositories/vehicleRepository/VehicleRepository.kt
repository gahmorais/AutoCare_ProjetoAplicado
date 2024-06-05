package br.com.gabrielmorais.autocare.data.repositories.vehicleRepository

import br.com.gabrielmorais.autocare.data.dao.VehicleDao
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {
  suspend fun create(vehicle: Vehicle) = try {
    vehicleDao.create(vehicle)
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(vehicle: Vehicle) = try {
    vehicleDao.update(vehicle)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getById(vehicleId: String) = try {
    vehicleDao.getById(vehicleId)
  } catch (e: Exception) {
    throw e
  }

  suspend fun delete(vehicle: Vehicle) = try {
    vehicleDao.delete(vehicle)
  } catch (e: Exception) {
    throw e
  }

  fun getMaintenances(vehicleId: String): Flow<List<Maintenance>> = try {
    vehicleDao.getMaintenances(vehicleId)
  } catch (e: Exception) {
    throw e
  }

}
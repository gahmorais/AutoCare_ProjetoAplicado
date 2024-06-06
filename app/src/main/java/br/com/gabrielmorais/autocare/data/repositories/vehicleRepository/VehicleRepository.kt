package br.com.gabrielmorais.autocare.data.repositories.vehicleRepository

import br.com.gabrielmorais.autocare.data.dao.VehicleDao
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) : IVehicleRepository {
  override suspend fun create(vehicle: Vehicle): Unit = try {
    vehicleDao.create(vehicle)
  } catch (e: Exception) {
    throw e
  }

  override suspend fun update(vehicle: Vehicle): Int = try {
    vehicleDao.update(vehicle)
  } catch (e: Exception) {
    throw e
  }

  override suspend fun getById(vehicleId: String): Vehicle = try {
    vehicleDao.getById(vehicleId)
  } catch (e: Exception) {
    throw e
  }

  override suspend fun delete(vehicle: Vehicle): Unit = try {
    vehicleDao.delete(vehicle)
  } catch (e: Exception) {
    throw e
  }

  override fun getMaintenances(vehicleId: String): Flow<List<Maintenance>> = try {
    vehicleDao.getMaintenances(vehicleId)
  } catch (e: Exception) {
    throw e
  }

}
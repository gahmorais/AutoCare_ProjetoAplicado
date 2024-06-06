package br.com.gabrielmorais.autocare.data.repositories.maintenance

import br.com.gabrielmorais.autocare.data.dao.MaintenanceDao
import br.com.gabrielmorais.autocare.data.models.Maintenance

class MaintenanceRepository(private val maintenanceDao: MaintenanceDao) : IMaintenanceRepository {
  override suspend fun create(maintenance: Maintenance) = try {
    maintenanceDao.create(maintenance)
  } catch (e: Exception) {
    throw e
  }

  override suspend fun getById(id: String): Maintenance = try {
    maintenanceDao.getById(id)
  } catch (e: Exception) {
    throw e
  }

  override suspend fun update(maintenance: Maintenance): Int = try {
    maintenanceDao.update(maintenance)
  } catch (e: Exception) {
    throw e
  }


  override suspend fun delete(maintenance: Maintenance) = try {
    maintenanceDao.delete(maintenance)
  } catch (e: Exception) {
    throw e
  }
}
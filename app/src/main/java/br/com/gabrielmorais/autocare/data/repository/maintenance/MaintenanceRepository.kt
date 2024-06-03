package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.dao.MaintenanceDao
import br.com.gabrielmorais.autocare.data.models.Maintenance

class MaintenanceRepository(private val maintenanceDao: MaintenanceDao) {
  suspend fun create(maintenance: Maintenance) = try {
    maintenanceDao.create(maintenance)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getById(id: String) = try {
    maintenanceDao.getById(id)
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(maintenance: Maintenance) = try {
    maintenanceDao.update(maintenance)
  } catch (e: Exception) {
    throw e
  }


  suspend fun delete(maintenance: Maintenance) = try {
    maintenanceDao.delete(maintenance)
  } catch (e: Exception) {
    throw e
  }
}
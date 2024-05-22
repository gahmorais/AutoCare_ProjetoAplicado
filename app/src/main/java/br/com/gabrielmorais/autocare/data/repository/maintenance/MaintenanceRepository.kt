package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.Maintenance

class MaintenanceRepository(private val database: AppDatabase) {
  suspend fun create(maintenance: Maintenance) = try {
    database.maintenanceDao().create(maintenance)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getById(id: String) = try {
    database.maintenanceDao().getById(id)
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(maintenance: Maintenance) = try {
    database.maintenanceDao().update(maintenance)
  } catch (e: Exception) {
    throw e
  }


  suspend fun delete(maintenance: Maintenance) = try {
    database.maintenanceDao().delete(maintenance)
  } catch (e: Exception) {
    throw e
  }
}
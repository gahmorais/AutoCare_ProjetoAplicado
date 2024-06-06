package br.com.gabrielmorais.autocare.data.repositories.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance

interface IMaintenanceRepository {
  suspend fun create(maintenance: Maintenance)
  suspend fun getById(id: String): Maintenance
  suspend fun update(maintenance: Maintenance):Int
  suspend fun delete(maintenance: Maintenance)
}
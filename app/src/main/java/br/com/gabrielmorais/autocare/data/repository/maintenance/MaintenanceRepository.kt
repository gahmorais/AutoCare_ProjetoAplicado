package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance

interface MaintenanceRepository {
  fun create(
    userId: String,
    vehicleId: String,
    maintenance: Maintenance,
    callback: (String) -> Unit
  )
}
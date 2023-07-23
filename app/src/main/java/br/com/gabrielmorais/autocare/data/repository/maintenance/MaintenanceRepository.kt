package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Vehicle

interface MaintenanceRepository {
  fun create(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  fun delete(
    userId: String,
    vehicleId: String,
    maintenanceId: Int
  )
}
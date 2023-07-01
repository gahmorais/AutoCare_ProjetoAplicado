package br.com.gabrielmorais.autocare.data.repository.vehicleRepository

import br.com.gabrielmorais.autocare.data.models.Vehicle

interface VehicleRepository {
  suspend fun saveVehicleImage(
    userId: String,
    vehicleId: String,
    image: ByteArray,
    callback: (String) -> Unit
  )
  fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSucess: (Vehicle) -> Unit,
    onError: (Throwable) -> Unit
  )
}
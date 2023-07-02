package br.com.gabrielmorais.autocare.data.repository.vehicleRepository

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.Vehicle

interface VehicleRepository {
  suspend fun saveVehicleImage(
    userId: String,
    vehicleId: String,
    image: Uri,
    callback: (String) -> Unit
  )
  fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSuccess: (Vehicle) -> Unit,
    onError: (Throwable) -> Unit
  )
  fun updateVehicle(
    userId:String,
    vehicleId:String,
    vehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )
}
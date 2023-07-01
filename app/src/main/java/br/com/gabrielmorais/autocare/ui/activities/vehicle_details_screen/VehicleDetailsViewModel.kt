package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepositoryImpl
) : ViewModel() {

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  val vehicle = _vehicle.asStateFlow()
  fun uploadVehiclePhoto(userId: String, vehicleId: String, image: Uri) {
  }

  fun getVehicle(userId: String, vehicleId: String) {
    Log.i("VehicleDetailsViewModel", "getVehicle: ${userId} | $vehicleId")
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSucess = {
        Log.i("VehicleDetailsViewModel", "getVehicle: ${it.model}")
        viewModelScope.launch { _vehicle.emit(it) }
      },
      onError = {
        Log.i("VehicleDetailsViewModel", "getVehicle: ${it.message}")
      }
    )
  }
}

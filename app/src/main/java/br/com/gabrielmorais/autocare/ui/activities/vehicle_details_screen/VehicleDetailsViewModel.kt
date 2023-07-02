package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepositoryImpl
) : ViewModel() {

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  private val _userId = MutableStateFlow("")
  val userId = _userId.asStateFlow()

  val vehicle = _vehicle.asStateFlow()
  fun uploadVehiclePhoto(userId: String, vehicleId: String, image: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      vehicleRepository.saveVehicleImage(userId, vehicleId, image) {
        viewModelScope.launch {
          updateVehicle(
            userId = userId,
            _vehicle.value!!.copy(photo = it)
          )
        }
      }
    }
  }

  fun getVehicle(userId: String, vehicleId: String) {
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSuccess = {
        viewModelScope.launch { _vehicle.emit(it) }
      },
      onError = {
        Log.i("VehicleDetailsViewModel", "getVehicle: ${it.message}")
      }
    )
  }

  private fun updateVehicle(userId: String, vehicle: Vehicle) {
    vehicleRepository.updateVehicle(
      userId,
      vehicle.id!!,
      vehicle,
      onSuccess = {
        viewModelScope.launch {
          _vehicle.emit(vehicle)
        }
      },
      onError = {
        Log.i("VehicleDetailsViewModel", "updateVehicle: ${it.message}")
      }
    )
  }

  fun setUserid(id: String) {
    _userId.value = id
  }
}

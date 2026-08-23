package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepository
) : ViewModel() {

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  private val _userId = MutableStateFlow("")
  val userId = _userId.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  val vehicle = _vehicle.asStateFlow()

  fun uploadVehiclePhoto(userId: String, vehicleId: String, image: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      // saveVehicleImage propaga a excecao do upload; sem o catch ela escapa
      // do viewModelScope e derruba o app.
      runCatching {
        vehicleRepository.saveVehicleImage(userId, vehicleId, image) { imageUrl ->
          val current = _vehicle.value ?: return@saveVehicleImage
          updateVehicle(userId = userId, vehicle = current.copy(photo = imageUrl))
        }
      }.onFailure(::publishError)
    }
  }

  fun getVehicle(userId: String, vehicleId: String) {
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSuccess = { _vehicle.value = it },
      onError = ::publishError
    )
  }

  private fun updateVehicle(userId: String, vehicle: Vehicle) {
    val vehicleId = vehicle.id
    if (vehicleId == null) {
      publishError(IllegalArgumentException("Veículo sem identificador"))
      return
    }
    vehicleRepository.updateVehicle(
      userId,
      vehicleId,
      vehicle,
      onSuccess = { _vehicle.value = vehicle },
      onError = ::publishError
    )
  }

  fun setUserid(id: String) {
    _userId.value = id
  }

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

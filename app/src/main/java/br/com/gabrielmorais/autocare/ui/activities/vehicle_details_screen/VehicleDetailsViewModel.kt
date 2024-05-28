package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryFirebase
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.handleException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class VehicleDetailsViewModel(
  private val userRepository: UserRepository,
  private val imageUtils: ImageUtils
) : ViewModel() {

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  val vehicle = _vehicle.asStateFlow()

  private val _message = MutableSharedFlow<String>()
  val message = _message.asSharedFlow()

  suspend fun uploadVehiclePhoto(vehicleId: String, image: Uri) = try {
    val imagePath = imageUtils.saveImage(vehicleId, image)
    val vehicleUpdated = _vehicle.value?.copy(photo = imagePath)
      ?: throw Exception("Veiculo nulo")
    userRepository.addVehicle(vehicleUpdated)
    _vehicle.update { vehicleUpdated }
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  suspend fun getVehicle(vehicleId: String) = try {
    val vehicle = userRepository.getVehicleById(vehicleId)
    _vehicle.emit(vehicle)
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  private fun emitMessage(text: String) = viewModelScope.launch {
    _message.emit(text)
  }

  private fun updateVehicle(userId: String, vehicle: Vehicle) {
  }

}

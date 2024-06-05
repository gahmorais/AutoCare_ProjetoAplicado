package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repositories.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repositories.vehicleRepository.VehicleRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.handleException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val imageUtils: ImageUtils
) : ViewModel() {

  private val _maintenances = MutableStateFlow<List<Maintenance>>(listOf())
  val maintenances = _maintenances.asStateFlow()

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  val vehicle = _vehicle.asStateFlow()

  private val _message = MutableSharedFlow<String>()
  val message = _message.asSharedFlow()

  suspend fun uploadVehiclePhoto(vehicleId: String, image: Uri) = try {
    val imagePath = imageUtils.saveImage(vehicleId, image)
    val vehicleUpdated = _vehicle.value?.copy(photo = imagePath)
      ?: throw Exception("Veiculo nulo")
    vehicleRepository.update(vehicleUpdated)
    _vehicle.update { vehicleUpdated }

  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  suspend fun deleteMaintenance(maintenance: Maintenance) = try {
    maintenanceRepository.delete(maintenance)
  } catch (e: Exception) {
    e.printStackTrace()
  }

  suspend fun getVehicle(vehicleId: String) = try {

    val vehicle = vehicleRepository.getById(vehicleId)
    _vehicle.update { vehicle }

    vehicleRepository.getMaintenances(vehicleId).onEach {
      _maintenances.emit(it)
    }.launchIn(viewModelScope)

  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  private fun emitMessage(text: String) = viewModelScope.launch {
    _message.emit(text)
  }
}

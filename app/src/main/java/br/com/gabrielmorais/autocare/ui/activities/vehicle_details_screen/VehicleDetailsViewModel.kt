package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepository,
  private val authRepository: AuthRepository,
  private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

  private val _vehicle = MutableStateFlow<Vehicle?>(null)

  // Derivado da sessao autenticada, nao de um extra de Intent forjavel.
  private val _userId = MutableStateFlow(authRepository.getCurrentUser()?.uid.orEmpty())
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

  fun getVehicle(vehicleId: String) {
    val userId = _userId.value
    if (userId.isBlank()) {
      publishError(IllegalStateException("Sessão expirada"))
      return
    }
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSuccess = { _vehicle.value = it },
      onError = ::publishError
    )
  }

  fun deleteMaintenance(maintenance: Maintenance, onDeleted: (Maintenance) -> Unit) {
    val userId = _userId.value
    val vehicleId = _vehicle.value?.id
    if (userId.isBlank() || vehicleId == null) {
      publishError(IllegalStateException("Não foi possível remover a manutenção"))
      return
    }
    maintenanceRepository.delete(
      userId = userId,
      vehicleId = vehicleId,
      maintenanceId = maintenance.id,
      onSuccess = {
        _message.value = it
        // Cancela tambem o alarme, senao a notificacao chegaria para um
        // registro que nao existe mais.
        onDeleted(maintenance)
      },
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

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AddMaintenanceViewModel(
  private val servicesRepository: ServicesRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val vehicleRepository: VehicleRepository,
  private val authRepository: AuthRepository
) :
  ViewModel() {
  private val _services = MutableStateFlow<List<Service?>>(listOf())
  val services = _services.asStateFlow()

  private val _servicesLoading = MutableStateFlow(true)
  val servicesLoading = _servicesLoading.asStateFlow()

  // Derivado da sessao autenticada, nao de um extra de Intent forjavel.
  private val _userId = MutableStateFlow(authRepository.currentUserId().orEmpty())
  val userId = _userId.asStateFlow()

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  val vehicle = _vehicle.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  init {
    getServices()
  }

  private fun getServices() {
    _servicesLoading.value = true
    servicesRepository.getServices(
      onSuccess = {
        _servicesLoading.value = false
        _services.value = it
      },
      onError = {
        _servicesLoading.value = false
        _message.value = it.message ?: "Não foi possível carregar os tipos de serviço"
      }
    )
  }

  /**
   * [onSaved] so e chamado quando a gravacao confirma. Antes a notificacao era
   * agendada em paralelo com a escrita e sobrevivia mesmo quando ela falhava.
   */
  fun saveMaintenance(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle,
    onSaved: () -> Unit
  ) {
    maintenanceRepository.create(
      userId,
      vehicleId,
      updatedVehicle,
      onSuccess = {
        _message.value = it
        onSaved()
      },
      onError = {
        _message.value = it.message ?: "Não foi possível salvar a manutenção"
      }
    )
  }

  fun getVehicle(vehicleId: String) {
    val userId = _userId.value
    if (userId.isBlank()) {
      _message.value = "Sessão expirada"
      return
    }
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSuccess = {
        _vehicle.value = it
      },
      onError = { _message.value = it.message ?: "Não foi possível carregar o veículo" })
  }
}
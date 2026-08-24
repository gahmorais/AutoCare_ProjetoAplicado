package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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

  /**
   * A rota carrega apenas o id: Navigation Compose nao passa Parcelable em
   * argumento. A manutencao e resolvida quando o veiculo chega, entao os dois
   * fluxos precisam ser combinados - qual dos dois chega primeiro nao importa.
   */
  private val _editingMaintenanceId = MutableStateFlow<Int?>(null)

  val editingMaintenance: StateFlow<Maintenance?> =
    combine(_vehicle, _editingMaintenanceId) { vehicle, id ->
      if (id == null) null else vehicle?.maintenances?.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

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

  fun startEditing(maintenanceId: Int) {
    _editingMaintenanceId.value = maintenanceId
  }

  /**
   * Como [saveMaintenance], so chama [onSaved] quando a gravacao confirma, para
   * a notificacao nunca ser reagendada por uma escrita que falhou.
   */
  fun updateMaintenance(
    vehicleId: String,
    maintenance: Maintenance,
    onSaved: () -> Unit
  ) {
    val userId = _userId.value
    if (userId.isBlank()) {
      _message.value = "Sessão expirada"
      return
    }
    maintenanceRepository.update(
      userId = userId,
      vehicleId = vehicleId,
      maintenance = maintenance,
      onSuccess = {
        _message.value = it
        onSaved()
      },
      onError = {
        _message.value = it.message ?: "Não foi possível atualizar a manutenção"
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
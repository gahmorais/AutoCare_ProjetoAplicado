package br.com.geasoftware.autocare.ui.activities.add_maintenance_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.geasoftware.autocare.data.local.repository.MaintenanceRepository
import br.com.geasoftware.autocare.data.local.repository.ServicesRepository
import br.com.geasoftware.autocare.data.local.repository.VehicleRepository
import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Service
import br.com.geasoftware.autocare.data.models.Vehicle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AddMaintenanceViewModel(
  private val servicesRepository: ServicesRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val vehicleRepository: VehicleRepository
) : ViewModel() {
  private val _services = MutableStateFlow<List<Service>>(emptyList())
  val services = _services.asStateFlow()

  private val _servicesLoading = MutableStateFlow(true)
  val servicesLoading = _servicesLoading.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  private val vehicleId = MutableStateFlow<String?>(null)

  val vehicle: StateFlow<Vehicle?> = vehicleId
    .filterNotNull()
    .flatMapLatest { id -> vehicleRepository.observeVehicle(id) }
    .catch { _message.value = it.message ?: "Não foi possível carregar o veículo" }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  /**
   * A rota carrega apenas o id: Navigation Compose nao passa Parcelable em
   * argumento. A manutencao e resolvida quando o veiculo chega, entao os dois
   * fluxos precisam ser combinados - qual dos dois chega primeiro nao importa.
   */
  private val _editingMaintenanceId = MutableStateFlow<Int?>(null)

  val editingMaintenance: StateFlow<Maintenance?> =
    combine(vehicle, _editingMaintenanceId) { vehicle, id ->
      if (id == null) null else vehicle?.maintenances?.firstOrNull { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  init {
    loadServices()
  }

  private fun loadServices() {
    viewModelScope.launch {
      _servicesLoading.value = true
      runCatching { servicesRepository.getServices() }
        .onSuccess { _services.value = it }
        .onFailure { _message.value = it.message ?: "Não foi possível carregar os tipos de serviço" }
      _servicesLoading.value = false
    }
  }

  fun getVehicle(vehicleId: String) {
    this.vehicleId.value = vehicleId
  }

  fun startEditing(maintenanceId: Int) {
    _editingMaintenanceId.value = maintenanceId
  }

  /**
   * Cria ou atualiza pelo id da manutencao. [onSaved] so e chamado quando a
   * gravacao confirma, para a notificacao nunca ser (re)agendada por uma
   * escrita que falhou.
   */
  fun saveMaintenance(vehicleId: String, maintenance: Maintenance, onSaved: () -> Unit) {
    viewModelScope.launch {
      runCatching { maintenanceRepository.saveMaintenance(vehicleId, maintenance) }
        .onSuccess {
          _message.value = "Manutenção salva com sucesso"
          onSaved()
        }
        .onFailure { _message.value = it.message ?: "Não foi possível salvar a manutenção" }
    }
  }
}

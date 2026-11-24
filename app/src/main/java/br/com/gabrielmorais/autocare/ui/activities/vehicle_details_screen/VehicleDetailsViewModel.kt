package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val imageStore: LocalImageStore
) : ViewModel() {

  private val vehicleId = MutableStateFlow<String?>(null)

  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  /**
   * Observado, nao lido uma vez: excluir, desfazer e trocar a foto atualizam a
   * tela sozinhos, sem a ViewModel remendar o estado na mao.
   */
  val vehicle: StateFlow<Vehicle?> = vehicleId
    .filterNotNull()
    .flatMapLatest { id -> vehicleRepository.observeVehicle(id) }
    .catch { publishError(it) }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  fun getVehicle(vehicleId: String) {
    this.vehicleId.value = vehicleId
  }

  /** A foto anterior vira orfa e e recolhida pela limpeza de fotos. */
  fun uploadVehiclePhoto(image: Uri) {
    viewModelScope.launch {
      runCatching {
        val current = vehicle.value ?: return@runCatching
        val path = imageStore.save(image, ImageKind.VEHICLE_PHOTO)
        vehicleRepository.saveVehicle(current.copy(photo = path))
      }.onFailure(::publishError)
    }
  }

  /** [onDeleted] cancela o alarme e oferece o desfazer. */
  fun deleteMaintenance(maintenance: Maintenance, onDeleted: (Maintenance) -> Unit) {
    viewModelScope.launch {
      runCatching { maintenanceRepository.deleteMaintenance(maintenance.id) }
        .onSuccess { onDeleted(maintenance) }
        .onFailure(::publishError)
    }
  }

  /**
   * Desfaz uma exclusao regravando com o mesmo id - o [onRestored] reagenda o
   * lembrete que a exclusao tinha cancelado.
   */
  fun restoreMaintenance(maintenance: Maintenance, onRestored: (Maintenance) -> Unit) {
    val currentVehicleId = vehicleId.value
      ?: return publishError(IllegalStateException("Não foi possível restaurar a manutenção"))
    viewModelScope.launch {
      runCatching { maintenanceRepository.saveMaintenance(currentVehicleId, maintenance) }
        .onSuccess { onRestored(maintenance) }
        .onFailure(::publishError)
    }
  }

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

package br.com.geasoftware.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.geasoftware.autocare.data.images.ImageKind
import br.com.geasoftware.autocare.data.images.LocalImageStore
import br.com.geasoftware.autocare.data.local.repository.MaintenanceRepository
import br.com.geasoftware.autocare.data.local.repository.ProfileRepository
import br.com.geasoftware.autocare.data.local.repository.VehicleRepository
import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
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
class VehicleDetailsViewModel(
  private val vehicleRepository: VehicleRepository,
  private val maintenanceRepository: MaintenanceRepository,
  private val imageStore: LocalImageStore,
  private val profileRepository: ProfileRepository
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

  /** Para quem o veiculo pode ser transferido: todos os perfis menos o dono. */
  val transferTargets: StateFlow<List<Profile>> =
    combine(profileRepository.observeProfiles(), vehicle) { profiles, current ->
      profiles.filter { it.id != current?.profileId }
    }
      .catch { publishError(it) }
      .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  /**
   * Passa o veiculo, com todo o historico, para [target] - ex.: o carro do pai
   * que ficou para o filho. Os alarmes nao mudam: o id das manutencoes e o mesmo.
   */
  fun transferVehicle(target: Profile, onTransferred: (Profile) -> Unit = {}) {
    val currentId = vehicleId.value
      ?: return publishError(IllegalStateException("Veículo não encontrado"))
    viewModelScope.launch {
      runCatching { vehicleRepository.transferVehicle(currentId, target.id) }
        .onSuccess { onTransferred(target) }
        .onFailure(::publishError)
    }
  }

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

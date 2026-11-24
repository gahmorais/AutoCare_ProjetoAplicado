package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.local.ActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.repository.ProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** O que a casca do app mostra ao abrir. */
enum class StartState {
  /** Ainda lendo o banco: a splash continua na tela. */
  LOADING,

  /** Nenhum perfil no aparelho: primeiro acesso. */
  NEEDS_ONBOARDING,
  READY
}

/**
 * ViewModel da casca (abas Inicio, Veiculos e Perfil). Tudo local: nao ha
 * sessao, e o "dono" dos dados e o perfil ativo.
 *
 * Erros viram [message] e nunca escapam do viewModelScope - uma excecao ali
 * derruba o app.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
  private val profileRepository: ProfileRepository,
  private val vehicleRepository: VehicleRepository,
  private val activeProfileStore: ActiveProfileStore,
  private val imageStore: LocalImageStore
) : ViewModel() {

  private val _message = MutableStateFlow("")
  val message: Flow<String> = _message

  /** null enquanto a primeira leitura do banco nao chega. */
  private val loadedProfiles: StateFlow<List<Profile>?> = profileRepository.observeProfiles()
    .catch { error ->
      publishError(error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  /** Todos os perfis do aparelho, em ordem de criacao. */
  val profiles: StateFlow<List<Profile>> = loadedProfiles
    .map { it.orEmpty() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val startState: StateFlow<StartState> = loadedProfiles
    .map { list ->
      when {
        list == null -> StartState.LOADING
        list.isEmpty() -> StartState.NEEDS_ONBOARDING
        else -> StartState.READY
      }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, StartState.LOADING)

  /**
   * O perfil guardado como ativo ou, se ele nao existir mais, o primeiro. Assim
   * um id orfao no DataStore nunca deixa o app sem dono.
   */
  val activeProfile: StateFlow<Profile?> =
    combine(loadedProfiles, activeProfileStore.activeProfileId) { list, activeId ->
      list.orEmpty().let { all -> all.firstOrNull { it.id == activeId } ?: all.firstOrNull() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

  val vehicles: StateFlow<List<Vehicle>> = activeProfile
    .map { it?.id }
    .distinctUntilChanged()
    .flatMapLatest { profileId ->
      if (profileId == null) flowOf(emptyList()) else vehicleRepository.observeVehicles(profileId)
    }
    .catch { error ->
      publishError(error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  /** Veiculos de todos os perfis: alimenta o Inicio e as contagens dos perfis. */
  val allVehicles: StateFlow<List<Vehicle>> = vehicleRepository.observeAllVehicles()
    .catch { error ->
      publishError(error)
      emit(emptyList())
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  private val _homeFilter = MutableStateFlow<String?>(null)

  /** Perfil filtrado no Inicio; null e "Todos", o padrao. */
  val homeFilter: StateFlow<String?> = _homeFilter

  /**
   * O Inicio mostra as pendencias de todos os perfis: quem cuida dos carros da
   * familia quer ver tudo o que vence num lugar so. O filtro restringe a um.
   */
  val homeVehicles: StateFlow<List<Vehicle>> =
    combine(allVehicles, _homeFilter) { vehicles, filter ->
      if (filter == null) vehicles else vehicles.filter { it.profileId == filter }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  /** Nome de cada perfil por id - vazio com um perfil so, quando o dono e obvio. */
  val ownerNames: StateFlow<Map<String, String>> = profiles
    .map { list -> if (list.size > 1) list.associate { it.id to it.name } else emptyMap() }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

  fun setHomeFilter(profileId: String?) {
    _homeFilter.value = profileId
  }

  fun selectProfile(profileId: String) {
    viewModelScope.launch {
      runCatching { activeProfileStore.setActiveProfileId(profileId) }.onFailure(::publishError)
    }
  }

  /**
   * Cria um perfil e o torna ativo - no primeiro acesso e ao adicionar outro.
   * A foto e opcional e, se falhar, o perfil e criado sem ela: perder a foto e
   * melhor do que prender a pessoa no formulario.
   */
  fun createProfile(name: String, photo: Uri? = null) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return publishError(IllegalArgumentException("Informe seu nome"))
    viewModelScope.launch {
      runCatching {
        val photoPath = photo?.let { uri ->
          runCatching { imageStore.save(uri, ImageKind.PROFILE_PHOTO) }
            .onFailure(::publishError)
            .getOrNull()
        }
        val profile = Profile(name = trimmed, photoPath = photoPath)
        profileRepository.saveProfile(profile)
        activeProfileStore.setActiveProfileId(profile.id)
      }.onFailure(::publishError)
    }
  }

  /**
   * Exclui o perfil com os veiculos e manutencoes dele. O ultimo perfil nao pode
   * sair: o app sempre precisa de um dono para os dados.
   *
   * [onDeleted] recebe os veiculos que sairam junto, para cancelar os alarmes
   * das manutencoes deles. Nao ha desfazer - a tela confirma antes.
   */
  fun deleteProfile(profileId: String, onDeleted: (List<Vehicle>) -> Unit = {}) {
    val all = profiles.value
    if (all.size <= 1) return publishError(IllegalStateException("É preciso manter ao menos um perfil"))
    val removedVehicles = allVehicles.value.filter { it.profileId == profileId }
    val wasActive = activeProfile.value?.id == profileId
    viewModelScope.launch {
      runCatching {
        profileRepository.deleteProfile(profileId)
        if (wasActive) all.firstOrNull { it.id != profileId }?.let { activeProfileStore.setActiveProfileId(it.id) }
        if (_homeFilter.value == profileId) _homeFilter.value = null
      }
        .onSuccess { onDeleted(removedVehicles) }
        .onFailure(::publishError)
    }
  }

  fun updateProfileName(name: String) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return publishError(IllegalArgumentException("Informe seu nome"))
    val current = activeProfile.value ?: return publishError(noActiveProfile())
    viewModelScope.launch {
      runCatching { profileRepository.saveProfile(current.copy(name = trimmed)) }
        .onSuccess { _message.value = "Perfil atualizado" }
        .onFailure(::publishError)
    }
  }

  /** A foto antiga vira orfa e e recolhida pela limpeza de fotos. */
  fun updateProfilePhoto(image: Uri) {
    val current = activeProfile.value ?: return publishError(noActiveProfile())
    viewModelScope.launch {
      runCatching {
        val path = imageStore.save(image, ImageKind.PROFILE_PHOTO)
        profileRepository.saveProfile(current.copy(photoPath = path))
      }.onFailure(::publishError)
    }
  }

  /** O veiculo sempre fica com o perfil ativo como dono. */
  fun saveVehicle(vehicle: Vehicle) {
    val owner = activeProfile.value ?: return publishError(noActiveProfile())
    viewModelScope.launch {
      runCatching { vehicleRepository.saveVehicle(vehicle.copy(profileId = owner.id)) }
        .onSuccess { _message.value = "Veículo salvo com sucesso" }
        .onFailure(::publishError)
    }
  }

  /**
   * [onDeleted] recebe o veiculo como estava, com as manutencoes, para o
   * desfazer do snackbar regrava-lo inteiro com [restoreVehicle].
   */
  fun deleteVehicle(vehicle: Vehicle, onDeleted: (Vehicle) -> Unit = {}) {
    val vehicleId = vehicle.id
      ?: return publishError(IllegalArgumentException("Veículo sem identificador"))
    viewModelScope.launch {
      runCatching { vehicleRepository.deleteVehicle(vehicleId) }
        .onSuccess { onDeleted(vehicle) }
        .onFailure(::publishError)
    }
  }

  /** Desfaz [deleteVehicle]: volta o veiculo e as manutencoes, com os mesmos ids. */
  fun restoreVehicle(vehicle: Vehicle, onRestored: (Vehicle) -> Unit = {}) {
    viewModelScope.launch {
      runCatching { vehicleRepository.restoreVehicle(vehicle) }
        .onSuccess { onRestored(vehicle) }
        .onFailure(::publishError)
    }
  }

  private fun noActiveProfile() = IllegalStateException("Nenhum perfil selecionado")

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

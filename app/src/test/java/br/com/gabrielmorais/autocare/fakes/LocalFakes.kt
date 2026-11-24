package br.com.gabrielmorais.autocare.fakes

import android.net.Uri
import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.local.ActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.ProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.ServicesRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceReminder
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.IOException

/*
 * Fakes em memoria dos repositorios locais. Cada um aceita [failure] para
 * exercitar os caminhos de erro: a ViewModel tem de transformar a excecao em
 * mensagem, nunca deixa-la escapar do viewModelScope.
 */

class FakeProfileRepository(initial: List<Profile> = emptyList()) : ProfileRepository {
  val profiles = MutableStateFlow(initial)
  var failure: Throwable? = null

  override fun observeProfiles(): Flow<List<Profile>> = profiles
  override fun observeProfile(id: String): Flow<Profile?> =
    profiles.map { list -> list.firstOrNull { it.id == id } }

  override suspend fun getProfile(id: String) = profiles.value.firstOrNull { it.id == id }
  override suspend fun countProfiles() = profiles.value.size

  override suspend fun saveProfile(profile: Profile) {
    failure?.let { throw it }
    profiles.update { list -> list.filterNot { it.id == profile.id } + profile }
  }

  override suspend fun deleteProfile(id: String) {
    failure?.let { throw it }
    profiles.update { list -> list.filterNot { it.id == id } }
  }
}

class FakeVehicleRepository(initial: List<Vehicle> = emptyList()) : VehicleRepository {
  val vehicles = MutableStateFlow(initial)
  var failure: Throwable? = null

  override fun observeVehicles(profileId: String): Flow<List<Vehicle>> =
    vehicles.map { list -> list.filter { it.profileId == profileId } }

  override fun observeAllVehicles(): Flow<List<Vehicle>> = vehicles
  override fun observeVehicle(id: String): Flow<Vehicle?> =
    vehicles.map { list -> list.firstOrNull { it.id == id } }

  override suspend fun getVehicle(id: String) = vehicles.value.firstOrNull { it.id == id }

  override suspend fun saveVehicle(vehicle: Vehicle) {
    failure?.let { throw it }
    requireNotNull(vehicle.profileId) { "Veículo sem perfil dono" }
    // Como o Room: salvar o veiculo nao mexe nas manutencoes ja gravadas.
    val kept = getVehicle(vehicle.id!!)?.maintenances ?: emptyList()
    vehicles.update { list -> list.filterNot { it.id == vehicle.id } + vehicle.copy(maintenances = kept) }
  }

  override suspend fun deleteVehicle(id: String) {
    failure?.let { throw it }
    vehicles.update { list -> list.filterNot { it.id == id } }
  }

  override suspend fun restoreVehicle(vehicle: Vehicle) {
    failure?.let { throw it }
    vehicles.update { list -> list.filterNot { it.id == vehicle.id } + vehicle }
  }

  override suspend fun transferVehicle(vehicleId: String, toProfileId: String) {
    failure?.let { throw it }
    vehicles.update { list ->
      list.map { if (it.id == vehicleId) it.copy(profileId = toProfileId) else it }
    }
  }
}

/** Grava nas manutencoes do [vehicles], como o banco faria pela chave estrangeira. */
class FakeMaintenanceRepository(private val vehicles: FakeVehicleRepository) : MaintenanceRepository {
  var failure: Throwable? = null

  override suspend fun saveMaintenance(vehicleId: String, maintenance: Maintenance) {
    failure?.let { throw it }
    vehicles.vehicles.update { list ->
      list.map { vehicle ->
        if (vehicle.id != vehicleId) return@map vehicle
        val others = vehicle.maintenances.orEmpty().filterNot { it.id == maintenance.id }
        vehicle.copy(maintenances = others + maintenance)
      }
    }
  }

  override suspend fun getMaintenance(id: Int): Maintenance? =
    vehicles.vehicles.value.flatMap { it.maintenances.orEmpty() }.firstOrNull { it.id == id }

  override suspend fun deleteMaintenance(id: Int) {
    failure?.let { throw it }
    vehicles.vehicles.update { list ->
      list.map { it.copy(maintenances = it.maintenances.orEmpty().filterNot { m -> m.id == id }) }
    }
  }

  override suspend fun getPendingWithForecast(): List<Maintenance> =
    vehicles.vehicles.value.flatMap { it.maintenances.orEmpty() }
      .filter { !it.completed && it.forecastNextExchangeDate != null }

  // Sem perfis no fake: o lembrete e coberto pelo teste instrumentado do Room.
  override suspend fun getReminder(maintenanceId: Int): MaintenanceReminder? = null
}

class FakeActiveProfileStore(initial: String? = null) : ActiveProfileStore {
  override val activeProfileId = MutableStateFlow(initial)
  override suspend fun setActiveProfileId(id: String) {
    activeProfileId.value = id
  }
}

class FakeServicesRepository(
  private val services: List<Service> = emptyList(),
  private val failure: Throwable? = null
) : ServicesRepository {
  override suspend fun getServices(): List<Service> {
    failure?.let { throw it }
    return services
  }
}

/** JPEG minimo valido para o [LocalImageStore] aceitar. */
val FAKE_JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0)

/**
 * [LocalImageStore] real sobre pastas temporarias, com a compressao trocada:
 * Bitmap e stub na JVM. [failWith] simula a falha da compressao.
 */
fun fakeImageStore(
  root: File,
  dispatcher: CoroutineDispatcher,
  failWith: () -> Throwable? = { null }
) = LocalImageStore(
  rootDir = File(root, "files").apply { mkdirs() },
  cacheDir = File(root, "cache").apply { mkdirs() },
  compress = { _: Uri, _: ImageKind -> failWith()?.let { throw it } ?: FAKE_JPEG },
  ioDispatcher = dispatcher
)

fun ioFailure(message: String) = IOException(message)

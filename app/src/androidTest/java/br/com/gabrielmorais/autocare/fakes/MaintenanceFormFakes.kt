package br.com.gabrielmorais.autocare.fakes

import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.ServicesRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceReminder
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fakes minimos para compor a tela de manutencao sem banco. Existem no
 * androidTest porque a regressao que eles guardam - retorno antecipado dentro de
 * lambda de composable derrubando o Composer - so aparece compondo de verdade.
 */
class FakeServicesRepository(
  private val services: List<Service>? = emptyList(),
  private val error: Throwable? = null
) : ServicesRepository {
  override suspend fun getServices(): List<Service> {
    error?.let { throw it }
    // null nunca responde: a tela fica no carregamento, que e onde o crash morava.
    return services ?: awaitCancellation()
  }
}

class FakeVehicleRepository(private val vehicle: Vehicle? = null) : VehicleRepository {
  override fun observeVehicles(profileId: String): Flow<List<Vehicle>> = flowOf(listOfNotNull(vehicle))
  override fun observeAllVehicles(): Flow<List<Vehicle>> = flowOf(listOfNotNull(vehicle))
  override fun observeVehicle(id: String): Flow<Vehicle?> = flowOf(vehicle)
  override suspend fun getVehicle(id: String): Vehicle? = vehicle
  override suspend fun saveVehicle(vehicle: Vehicle) = Unit
  override suspend fun deleteVehicle(id: String) = Unit
  override suspend fun restoreVehicle(vehicle: Vehicle) = Unit
  override suspend fun transferVehicle(vehicleId: String, toProfileId: String) = Unit
}

class FakeMaintenanceRepository : MaintenanceRepository {
  /** O que a tela mandou gravar, na ordem. */
  val saved = mutableListOf<Maintenance>()

  override suspend fun saveMaintenance(vehicleId: String, maintenance: Maintenance) {
    saved += maintenance
  }
  override suspend fun getMaintenance(id: Int): Maintenance? = null
  override suspend fun deleteMaintenance(id: Int) = Unit
  override suspend fun getPendingWithForecast(): List<Maintenance> = emptyList()
  override suspend fun getReminder(maintenanceId: Int): MaintenanceReminder? = null
}

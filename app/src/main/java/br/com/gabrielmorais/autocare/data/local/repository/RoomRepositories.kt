package br.com.gabrielmorais.autocare.data.local.repository

import androidx.room.withTransaction
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.toDomain
import br.com.gabrielmorais.autocare.data.local.toEntity
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceReminder
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.models.displayName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomProfileRepository(database: AutoCareDatabase) : ProfileRepository {
  private val dao = database.profileDao()

  override fun observeProfiles(): Flow<List<Profile>> =
    dao.observeAll().map { profiles -> profiles.map { it.toDomain() } }

  override fun observeProfile(id: String): Flow<Profile?> =
    dao.observe(id).map { it?.toDomain() }

  override suspend fun getProfile(id: String): Profile? = dao.get(id)?.toDomain()

  override suspend fun countProfiles(): Int = dao.count()

  override suspend fun saveProfile(profile: Profile) = dao.upsert(profile.toEntity())

  override suspend fun deleteProfile(id: String) = dao.delete(id)
}

class RoomVehicleRepository(private val database: AutoCareDatabase) : VehicleRepository {
  private val vehicleDao = database.vehicleDao()
  private val maintenanceDao = database.maintenanceDao()

  override fun observeVehicles(profileId: String): Flow<List<Vehicle>> =
    vehicleDao.observeByProfile(profileId).map { vehicles -> vehicles.map { it.toDomain() } }

  override fun observeAllVehicles(): Flow<List<Vehicle>> =
    vehicleDao.observeAll().map { vehicles -> vehicles.map { it.toDomain() } }

  override fun observeVehicle(id: String): Flow<Vehicle?> =
    vehicleDao.observe(id).map { it?.toDomain() }

  override suspend fun getVehicle(id: String): Vehicle? = vehicleDao.get(id)?.toDomain()

  override suspend fun saveVehicle(vehicle: Vehicle) = vehicleDao.upsert(vehicle.toEntity())

  override suspend fun deleteVehicle(id: String) = vehicleDao.delete(id)

  override suspend fun restoreVehicle(vehicle: Vehicle) {
    val entity = vehicle.toEntity()
    database.withTransaction {
      vehicleDao.upsert(entity)
      maintenanceDao.upsertAll(vehicle.maintenances.orEmpty().map { it.toEntity(entity.id) })
    }
  }

  override suspend fun transferVehicle(vehicleId: String, toProfileId: String) {
    val updated = vehicleDao.updateOwner(vehicleId = vehicleId, profileId = toProfileId)
    if (updated == 0) throw NoSuchElementException("Veículo não encontrado")
  }
}

class RoomMaintenanceRepository(private val database: AutoCareDatabase) : MaintenanceRepository {
  private val dao = database.maintenanceDao()

  override suspend fun saveMaintenance(vehicleId: String, maintenance: Maintenance) =
    dao.upsert(maintenance.toEntity(vehicleId))

  override suspend fun getMaintenance(id: Int): Maintenance? = dao.get(id)?.toDomain()

  override suspend fun deleteMaintenance(id: Int) = dao.delete(id)

  override suspend fun getPendingWithForecast(): List<Maintenance> =
    dao.getPendingWithForecast().map { it.toDomain() }

  override suspend fun getReminder(maintenanceId: Int): MaintenanceReminder? =
    database.withTransaction {
      val maintenance = dao.get(maintenanceId) ?: return@withTransaction null
      val vehicle = database.vehicleDao().get(maintenance.vehicleId) ?: return@withTransaction null
      val owner = database.profileDao().get(vehicle.vehicle.profileId) ?: return@withTransaction null
      MaintenanceReminder(
        maintenance = maintenance.toDomain(),
        vehicleName = vehicle.toDomain().displayName(),
        ownerName = owner.name,
        profileCount = database.profileDao().count()
      )
    }
}

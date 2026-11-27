package br.com.geasoftware.autocare.data.local

import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
import br.com.geasoftware.autocare.data.models.Vehicle

fun ProfileEntity.toDomain() = Profile(
  id = id,
  name = name,
  photoPath = photoPath,
  createdAt = createdAt
)

fun Profile.toEntity() = ProfileEntity(
  id = id,
  name = name,
  photoPath = photoPath,
  createdAt = createdAt
)

fun VehicleWithMaintenances.toDomain() = Vehicle(
  id = vehicle.id,
  profileId = vehicle.profileId,
  nickName = vehicle.nickName,
  brand = vehicle.brand,
  model = vehicle.model,
  plate = vehicle.plate,
  photo = vehicle.photoPath,
  averageDistanceTraveledPerMonth = vehicle.averageDistancePerMonth,
  maintenances = maintenances.map { it.toDomain() }
)

/**
 * O modelo de dominio aceita id e dono nulos (a tela monta o veiculo antes de
 * saber o dono); o banco nao. Falhar aqui e melhor do que gravar um veiculo orfao.
 */
fun Vehicle.toEntity() = VehicleEntity(
  id = requireNotNull(id) { "Veículo sem identificador" },
  profileId = requireNotNull(profileId) { "Veículo sem perfil dono" },
  nickName = nickName,
  brand = brand,
  model = model,
  plate = plate,
  photoPath = photo,
  averageDistancePerMonth = averageDistanceTraveledPerMonth
)

fun MaintenanceEntity.toDomain() = Maintenance(
  id = id,
  description = description,
  date = date,
  currentMileage = currentMileage,
  forecastNextExchangeMileage = forecastNextExchangeMileage,
  forecastNextExchangeDate = forecastNextExchangeDate,
  comments = comments,
  completed = completed
)

fun Maintenance.toEntity(vehicleId: String) = MaintenanceEntity(
  id = id,
  vehicleId = vehicleId,
  description = description,
  date = date,
  currentMileage = currentMileage,
  forecastNextExchangeMileage = forecastNextExchangeMileage,
  forecastNextExchangeDate = forecastNextExchangeDate,
  comments = comments,
  completed = completed
)

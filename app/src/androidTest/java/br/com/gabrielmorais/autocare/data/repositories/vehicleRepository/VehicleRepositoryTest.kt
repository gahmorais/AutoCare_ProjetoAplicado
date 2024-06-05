package br.com.gabrielmorais.autocare.data.repositories.vehicleRepository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repositories.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repositories.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class VehicleRepositoryTest {

  private lateinit var vehicleRepository: VehicleRepository
  private lateinit var userRepository: UserRepository
  private lateinit var maintenanceRepository: MaintenanceRepository
  private lateinit var database: AppDatabase
  private val user = User(
    photo = null,
    nickname = "gabriel.morais",
    name = "Gabriel Morais",
    password = "1234"
  )
  private val vehiclesMock = listOf(
    Vehicle(
      userId = user.id,
      nickName = "Carrao",
      brand = "Ford",
      model = "GT500",
      plate = "ABC1234",
      photo = null,
      averageDistanceTraveledPerMonth = 500
    ),
    Vehicle(
      userId = user.id,
      nickName = "Trabalho",
      brand = "Fiat",
      model = "Uno",
      plate = "ABC5612",
      photo = null,
      averageDistanceTraveledPerMonth = 2000
    ),
    Vehicle(
      userId = user.id,
      nickName = "Viagem",
      brand = "Volkswagen",
      model = "Tiguan",
      plate = "ABC6543",
      photo = null,
      averageDistanceTraveledPerMonth = 10000
    )
  )

  @Before
  fun setUp() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    maintenanceRepository = MaintenanceRepository(database.maintenanceDao())
    vehicleRepository = VehicleRepository(database.vehicleDao())
    userRepository = UserRepository(database.userDao())
    withContext(testScheduler) {
      userRepository.createUser(user)
    }
  }

  @After
  fun closeDb() {
    database.close()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun createOneVehicle() = runTest {
    val vehicleMock = vehiclesMock[0]
    val vehicleFounded = withContext(testScheduler) {
      vehicleRepository.create(vehicleMock)
      vehicleRepository.getById(vehicleMock.id)
    }
    advanceUntilIdle()
    Assert.assertEquals(vehicleMock, vehicleFounded)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun createManyVehicles() = runTest {
    val vehicles = withContext(testScheduler) {
      vehiclesMock.forEach { vehicle ->
        vehicleRepository.create(vehicle)
      }
      userRepository.getVehicles(user.id).first()
    }
    advanceUntilIdle()
    Assert.assertEquals(vehicles.size, 3)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun updateVehicle() = runTest {
    val vehicleMock = vehiclesMock[0]
    val updatedVehicle = vehicleMock.copy(nickName = "Super Carro")

    val vehicle = withContext(testScheduler) {
      vehicleRepository.create(vehicleMock)
      vehicleRepository.update(updatedVehicle)
      vehicleRepository.getById(updatedVehicle.id)
    }

    advanceUntilIdle()
    Assert.assertEquals(updatedVehicle.nickName, vehicle.nickName)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun deleteVehicle() = runTest {
    val vehicle = vehiclesMock[0]
    val vehicleDeleted = withContext(testScheduler) {
      vehicleRepository.create(vehicle)
      vehicleRepository.delete(vehicle)
      vehicleRepository.getById(vehicle.id)
    }
    advanceUntilIdle()
    Assert.assertEquals(vehicleDeleted, null)
  }

  @Test
  @OptIn(ExperimentalCoroutinesApi::class)
  fun getMaintenances() = runTest {
    val vehicle = vehiclesMock[0]
    val maintenances = listOf(
      Maintenance(
        vehicleId = vehicle.id,
        description = "Troca de óleo",
        date = System.currentTimeMillis(),
        forecastNextExchangeDate = System.currentTimeMillis() + 1_000_000,
        currentMileage = 40000,
        forecastNextExchangeMileage = 50000,
      ),
      Maintenance(
        vehicleId = vehicle.id,
        description = "Troca de filtro de óleo",
        date = System.currentTimeMillis(),
        forecastNextExchangeDate = System.currentTimeMillis() + 1_000_000,
        currentMileage = 40000,
        forecastNextExchangeMileage = 50000,
      ),
      Maintenance(
        vehicleId = vehicle.id,
        description = "Troca filtro de ar",
        date = System.currentTimeMillis(),
        forecastNextExchangeDate = System.currentTimeMillis() + 1_000_000,
        currentMileage = 40000,
        forecastNextExchangeMileage = 50000,
      )
    )
    val carriedOutMaintenances = withContext(testScheduler) {
      vehicleRepository.create(vehicle)
      maintenances.forEach { maintenance ->
        maintenanceRepository.create(maintenance)
      }
      vehicleRepository.getMaintenances(vehicle.id).first()
    }
    advanceUntilIdle()
    Assert.assertEquals(maintenances, carriedOutMaintenances)
  }


}
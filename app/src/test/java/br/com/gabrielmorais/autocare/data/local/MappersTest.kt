package br.com.gabrielmorais.autocare.data.local

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class MappersTest {

  private val maintenance = Maintenance(
    id = 42,
    description = "Troca de óleo",
    date = 19_000L,
    currentMileage = 50_000,
    forecastNextExchangeMileage = 60_000,
    forecastNextExchangeDate = 19_365L,
    comments = "Óleo 5W30",
    completed = true
  )

  private val vehicle = Vehicle(
    id = "v1",
    profileId = "p1",
    nickName = "Gol",
    brand = "VW",
    model = "Gol 1.0",
    plate = "ABC1D23",
    photo = "images/abc.jpg",
    averageDistanceTraveledPerMonth = 1_200,
    maintenances = listOf(maintenance)
  )

  @Test
  fun `perfil faz ida e volta sem perder campo`() {
    val profile = Profile(id = "p1", name = "Ana", photoPath = "images/a.jpg", createdAt = 123L)
    assertEquals(profile, profile.toEntity().toDomain())
  }

  @Test
  fun `manutencao faz ida e volta e leva o veiculo dono`() {
    val entity = maintenance.toEntity(vehicleId = "v1")
    assertEquals("v1", entity.vehicleId)
    assertEquals(maintenance, entity.toDomain())
  }

  @Test
  fun `veiculo faz ida e volta com as manutencoes`() {
    val withMaintenances = VehicleWithMaintenances(
      vehicle = vehicle.toEntity(),
      maintenances = vehicle.maintenances!!.map { it.toEntity(vehicle.id!!) }
    )
    assertEquals(vehicle, withMaintenances.toDomain())
  }

  @Test
  fun `campos opcionais nulos sobrevivem a ida e volta`() {
    val bare = Vehicle(id = "v2", profileId = "p1", maintenances = emptyList())
    val back = VehicleWithMaintenances(bare.toEntity(), emptyList()).toDomain()
    assertEquals(bare, back)
    assertNull(back.photo)

    val bareMaintenance = Maintenance(id = 7)
    assertEquals(bareMaintenance, bareMaintenance.toEntity("v2").toDomain())
  }

  @Test
  fun `veiculo sem perfil dono nao vira entidade`() {
    val error = assertThrows(IllegalArgumentException::class.java) {
      vehicle.copy(profileId = null).toEntity()
    }
    assertEquals("Veículo sem perfil dono", error.message)
  }

  @Test
  fun `veiculo sem id nao vira entidade`() {
    assertThrows(IllegalArgumentException::class.java) {
      vehicle.copy(id = null).toEntity()
    }
  }
}

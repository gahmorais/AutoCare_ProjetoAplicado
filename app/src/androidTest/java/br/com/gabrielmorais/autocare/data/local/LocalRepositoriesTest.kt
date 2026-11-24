package br.com.gabrielmorais.autocare.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.data.local.repository.AssetServicesRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomVehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Banco real em memoria, exercitado pelos repositorios. runBlocking e nao
 * runTest: o Room despacha nos proprios executores, e o tempo virtual do
 * runTest faria o withTimeout estourar antes da consulta voltar.
 */
@RunWith(AndroidJUnit4::class)
class LocalRepositoriesTest {

  private lateinit var database: AutoCareDatabase
  private lateinit var profiles: RoomProfileRepository
  private lateinit var vehicles: RoomVehicleRepository
  private lateinit var maintenances: RoomMaintenanceRepository

  private val ana = Profile(id = "ana", name = "Ana", createdAt = 1L)
  private val pedro = Profile(id = "pedro", name = "Pedro", createdAt = 2L)

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    database = Room.inMemoryDatabaseBuilder(context, AutoCareDatabase::class.java).build()
    profiles = RoomProfileRepository(database)
    vehicles = RoomVehicleRepository(database)
    maintenances = RoomMaintenanceRepository(database)
    runBlocking {
      profiles.saveProfile(ana)
      profiles.saveProfile(pedro)
    }
  }

  @After
  fun tearDown() = database.close()

  private fun vehicle(id: String, owner: String, name: String = id) =
    Vehicle(id = id, profileId = owner, nickName = name, maintenances = emptyList())

  private fun maintenance(id: Int, completed: Boolean = false, forecast: Long? = 20_000L) =
    Maintenance(id = id, description = "Serviço $id", completed = completed, forecastNextExchangeDate = forecast)

  @Test
  fun perfisSaoListadosEmOrdemDeCriacao() = runBlocking {
    assertEquals(listOf(ana, pedro), profiles.observeProfiles().first())
    assertEquals(2, profiles.countProfiles())
  }

  @Test
  fun excluirPerfilApagaVeiculosEManutencoesEmCascata() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    maintenances.saveMaintenance("gol", maintenance(1))
    vehicles.saveVehicle(vehicle("uno", "pedro"))

    profiles.deleteProfile("ana")

    assertNull(profiles.getProfile("ana"))
    assertNull(vehicles.getVehicle("gol"))
    assertNull(maintenances.getMaintenance(1))
    assertEquals(listOf("uno"), vehicles.observeAllVehicles().first().map { it.id })
  }

  @Test
  fun observarVeiculosTrazSoOsDoPerfilEReemiteAposInsercao() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    vehicles.saveVehicle(vehicle("uno", "pedro"))

    assertEquals(listOf("gol"), vehicles.observeVehicles("ana").first().map { it.id })

    // Coleta comeca antes da insercao: so passa se o Flow reemitir.
    val reemitted = async(start = CoroutineStart.UNDISPATCHED) {
      withTimeout(5_000) {
        vehicles.observeVehicles("ana").first { list -> list.any { it.id == "argo" } }
      }
    }
    vehicles.saveVehicle(vehicle("argo", "ana"))

    assertEquals(setOf("gol", "argo"), reemitted.await().map { it.id }.toSet())
  }

  @Test
  fun atualizarVeiculoNaoApagaAsManutencoes() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    maintenances.saveMaintenance("gol", maintenance(1))
    maintenances.saveMaintenance("gol", maintenance(2))

    // Com @Insert(REPLACE) a linha seria apagada e as manutencoes iriam em cascata.
    vehicles.saveVehicle(vehicle("gol", "ana", name = "Gol do trabalho"))

    val saved = vehicles.getVehicle("gol")!!
    assertEquals("Gol do trabalho", saved.nickName)
    assertEquals(setOf(1, 2), saved.maintenances!!.map { it.id }.toSet())
  }

  @Test
  fun atualizarPerfilNaoApagaOsVeiculos() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))

    profiles.saveProfile(ana.copy(name = "Ana Maria"))

    assertEquals("Ana Maria", profiles.getProfile("ana")!!.name)
    assertEquals(listOf("gol"), vehicles.observeVehicles("ana").first().map { it.id })
  }

  @Test
  fun transferirVeiculoMudaODonoEMantemAsManutencoes() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    maintenances.saveMaintenance("gol", maintenance(1))

    vehicles.transferVehicle("gol", toProfileId = "pedro")

    assertTrue(vehicles.observeVehicles("ana").first().isEmpty())
    val moved = vehicles.observeVehicles("pedro").first().single()
    assertEquals("pedro", moved.profileId)
    assertEquals(listOf(1), moved.maintenances!!.map { it.id })
  }

  @Test
  fun transferirParaPerfilInexistenteFalhaSemMudarODono() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))

    try {
      vehicles.transferVehicle("gol", toProfileId = "ninguem")
      fail("a chave estrangeira deveria recusar o perfil inexistente")
    } catch (expected: SQLiteConstraintException) {
    }
    assertEquals("ana", vehicles.getVehicle("gol")!!.profileId)
  }

  @Test
  fun transferirVeiculoInexistenteFalha() = runBlocking {
    try {
      vehicles.transferVehicle("fantasma", toProfileId = "pedro")
      fail("deveria falhar para veículo inexistente")
    } catch (expected: NoSuchElementException) {
    }
  }

  @Test
  fun restaurarVeiculoExcluidoDevolveAsManutencoes() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    maintenances.saveMaintenance("gol", maintenance(1))
    maintenances.saveMaintenance("gol", maintenance(2))
    val beforeDelete = vehicles.getVehicle("gol")!!

    vehicles.deleteVehicle("gol")
    assertNull(maintenances.getMaintenance(1))

    vehicles.restoreVehicle(beforeDelete)

    val restored = vehicles.getVehicle("gol")!!
    assertEquals(beforeDelete.nickName, restored.nickName)
    assertEquals(setOf(1, 2), restored.maintenances!!.map { it.id }.toSet())
  }

  @Test
  fun pendentesComPrevisaoVemDeTodosOsPerfis() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    vehicles.saveVehicle(vehicle("uno", "pedro"))
    maintenances.saveMaintenance("gol", maintenance(1))
    maintenances.saveMaintenance("uno", maintenance(2))
    maintenances.saveMaintenance("gol", maintenance(3, completed = true))
    maintenances.saveMaintenance("uno", maintenance(4, forecast = null))

    val pending = maintenances.getPendingWithForecast().map { it.id }.toSet()

    assertEquals(setOf(1, 2), pending)
  }

  @Test
  fun excluirEDesfazerManutencao() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    val original = maintenance(1)
    maintenances.saveMaintenance("gol", original)

    maintenances.deleteMaintenance(1)
    assertNull(maintenances.getMaintenance(1))

    maintenances.saveMaintenance("gol", original)
    assertEquals(original, maintenances.getMaintenance(1))
  }

  @Test
  fun catalogoDeServicosEmbarcadoCarrega() = runBlocking {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val services = AssetServicesRepository(context.assets).getServices()
    assertTrue(services.isNotEmpty())
  }

  @Test
  fun lembreteTrazVeiculoDonoEContagemDePerfis() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana", name = "Gol"))
    maintenances.saveMaintenance("gol", maintenance(1))

    val reminder = maintenances.getReminder(1)!!

    assertEquals(1, reminder.maintenance.id)
    assertEquals("Gol", reminder.vehicleName)
    assertEquals("Ana", reminder.ownerName)
    assertEquals(2, reminder.profileCount)
  }

  @Test
  fun lembreteAcompanhaTransferenciaDePerfil() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana", name = "Gol"))
    maintenances.saveMaintenance("gol", maintenance(1))

    vehicles.transferVehicle("gol", toProfileId = "pedro")

    assertEquals("Pedro", maintenances.getReminder(1)!!.ownerName)
  }

  @Test
  fun lembreteDeManutencaoExcluidaENulo() = runBlocking {
    vehicles.saveVehicle(vehicle("gol", "ana"))
    maintenances.saveMaintenance("gol", maintenance(1))
    maintenances.deleteMaintenance(1)

    assertNull(maintenances.getReminder(1))
  }
}

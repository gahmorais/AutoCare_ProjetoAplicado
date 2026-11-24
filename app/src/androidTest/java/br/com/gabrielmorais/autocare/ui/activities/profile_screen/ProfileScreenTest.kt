package br.com.gabrielmorais.autocare.ui.activities.profile_screen

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.local.ActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomVehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Tela de Perfil sobre o banco real em memoria: o que importa aqui e o caminho
 * inteiro - tocar, confirmar e ver o Room apagar em cascata.
 */
@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {

  @get:Rule
  val compose = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private lateinit var database: AutoCareDatabase
  private lateinit var profiles: RoomProfileRepository
  private lateinit var vehicles: RoomVehicleRepository
  private val activeStore = object : ActiveProfileStore {
    override val activeProfileId = MutableStateFlow<String?>("ana")
    override suspend fun setActiveProfileId(id: String) {
      activeProfileId.value = id
    }
  }
  private val workDir = File(context.cacheDir, "profile-screen-test")

  @Before
  fun setUp() {
    database = Room.inMemoryDatabaseBuilder(context, AutoCareDatabase::class.java).build()
    profiles = RoomProfileRepository(database)
    vehicles = RoomVehicleRepository(database)
    val maintenances = RoomMaintenanceRepository(database)
    runBlocking {
      profiles.saveProfile(Profile(id = "ana", name = "Ana", createdAt = 1L))
      profiles.saveProfile(Profile(id = "pedro", name = "Pedro", createdAt = 2L))
      vehicles.saveVehicle(Vehicle(id = "gol", profileId = "ana", nickName = "Gol"))
      maintenances.saveMaintenance("gol", Maintenance(id = 1))
      maintenances.saveMaintenance("gol", Maintenance(id = 2))
    }

    val viewModel = MainViewModel(
      profileRepository = profiles,
      vehicleRepository = vehicles,
      activeProfileStore = activeStore,
      imageStore = LocalImageStore(
        rootDir = File(workDir, "files"),
        cacheDir = File(workDir, "cache"),
        compress = { _, _ -> error("foto não faz parte deste teste") }
      )
    )
    compose.setContent { AutoCareTheme { ProfileScreen(viewModel) } }
    compose.waitUntil(5_000) {
      compose.onAllNodes(hasText("Em uso")).fetchSemanticsNodes().isNotEmpty()
    }
  }

  @After
  fun tearDown() {
    database.close()
    workDir.deleteRecursively()
  }

  @Test
  fun lista_os_perfis_com_a_contagem_de_veiculos() {
    compose.onNodeWithText("Pedro").assertExists()
    compose.onNodeWithText("1 veículo").assertExists()
    compose.onNodeWithText("0 veículos").assertExists()
  }

  @Test
  fun excluir_mostra_o_que_vai_junto_e_ativa_outro_perfil() {
    compose.onNodeWithContentDescription("Excluir perfil Ana").performClick()

    compose.onNodeWithText("Excluir Ana?").assertExists()
    compose.onNodeWithText("Também serão apagados 1 veículo e 2 manutenções deste perfil. Não é possível desfazer.")
      .assertExists()
    compose.onNodeWithText("Excluir").performClick()

    compose.waitUntil(5_000) { runBlocking { profiles.countProfiles() } == 1 }
    assertEquals("pedro", activeStore.activeProfileId.value)
    runBlocking { assertEquals(null, vehicles.getVehicle("gol")) }
  }

  @Test
  fun cancelar_a_exclusao_nao_apaga_nada() {
    compose.onNodeWithContentDescription("Excluir perfil Ana").performClick()
    compose.onNodeWithText("Cancelar").performClick()

    runBlocking { assertEquals(2, profiles.countProfiles()) }
  }

  @Test
  fun adicionar_perfil_cria_e_ativa() {
    compose.onNodeWithText("Adicionar perfil").performClick()
    compose.onAllNodes(hasText("Nome"))[1].performTextInput("Bia")
    compose.onNodeWithText("Criar").performClick()

    compose.waitUntil(5_000) { runBlocking { profiles.countProfiles() } == 3 }
    compose.waitUntil(5_000) {
      runBlocking { profiles.getProfile(activeStore.activeProfileId.value!!)?.name } == "Bia"
    }
  }
}

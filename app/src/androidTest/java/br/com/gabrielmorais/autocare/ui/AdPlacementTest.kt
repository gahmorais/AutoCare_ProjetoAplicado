package br.com.gabrielmorais.autocare.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.data.ads.LocalAdsController
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.local.ActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomVehicleRepository
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.fakes.FakeAdsController
import br.com.gabrielmorais.autocare.ui.activities.home_screen.HomeScreen
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.activities.onboarding_screen.OnboardingScreen
import br.com.gabrielmorais.autocare.ui.activities.profile_screen.ProfileScreen
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsRoute
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsViewModel
import br.com.gabrielmorais.autocare.ui.activities.vehicles_screen.VehiclesScreen
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * O banner so entra em Veiculos e no Detalhe do veiculo. Nada de anuncio no
 * Inicio, no Perfil (onde fica o backup) nem no primeiro acesso, onde um toque
 * acidental e facil. O formulario de manutencao e coberto em
 * MaintenanceFormScreenTest.
 */
@RunWith(AndroidJUnit4::class)
class AdPlacementTest {

  @get:Rule
  val compose = createComposeRule()

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private val workDir = File(context.cacheDir, "ad-placement-test")
  private val ads = FakeAdsController()
  private lateinit var database: AutoCareDatabase
  private lateinit var mainViewModel: MainViewModel
  private lateinit var detailsViewModel: VehicleDetailsViewModel

  @Before
  fun setUp() {
    database = Room.inMemoryDatabaseBuilder(context, AutoCareDatabase::class.java).build()
    val profiles = RoomProfileRepository(database)
    val vehicles = RoomVehicleRepository(database)
    runBlocking {
      profiles.saveProfile(Profile(id = "ana", name = "Ana", createdAt = 1L))
      vehicles.saveVehicle(
        Vehicle(id = "gol", profileId = "ana", nickName = "Gol", averageDistanceTraveledPerMonth = 1_000)
      )
    }
    val imageStore = LocalImageStore(
      rootDir = File(workDir, "files"),
      cacheDir = File(workDir, "cache"),
      compress = { _, _ -> error("foto não faz parte deste teste") }
    )
    val activeStore = object : ActiveProfileStore {
      override val activeProfileId = MutableStateFlow<String?>("ana")
      override suspend fun setActiveProfileId(id: String) {
        activeProfileId.value = id
      }
    }
    mainViewModel = MainViewModel(profiles, vehicles, activeStore, imageStore)
    detailsViewModel = VehicleDetailsViewModel(
      vehicleRepository = vehicles,
      maintenanceRepository = RoomMaintenanceRepository(database),
      imageStore = imageStore,
      profileRepository = profiles
    )
  }

  @After
  fun tearDown() {
    database.close()
    workDir.deleteRecursively()
  }

  private fun renderizar(esperarTexto: String, content: @Composable () -> Unit) {
    compose.setContent {
      AutoCareTheme {
        CompositionLocalProvider(LocalAdsController provides ads, content = content)
      }
    }
    compose.waitUntil(5_000) {
      compose.onAllNodes(hasText(esperarTexto, substring = true)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun banner() = compose.onNodeWithTag(FakeAdsController.BANNER_TAG)

  @Test
  fun veiculos_mostra_o_banner() {
    renderizar("Gol") { VehiclesScreen(mainViewModel, onOpenVehicle = {}, onManageProfiles = {}) }

    banner().assertExists()
  }

  @Test
  fun detalhe_do_veiculo_mostra_o_banner() {
    renderizar("Gol") {
      VehicleDetailsRoute(
        vehicleId = "gol",
        onBack = {},
        onAddMaintenance = {},
        onEditMaintenance = { _, _ -> },
        viewModel = detailsViewModel
      )
    }

    banner().assertExists()
  }

  @Test
  fun inicio_nao_mostra_anuncio() {
    renderizar("AutoCare") {
      HomeScreen(
        viewModel = mainViewModel,
        onOpenVehicle = {},
        onOpenMaintenance = { _, _ -> },
        onAddVehicle = {},
        onManageProfiles = {}
      )
    }

    banner().assertDoesNotExist()
  }

  @Test
  fun perfil_e_backup_nao_mostram_anuncio() {
    renderizar("Em uso") { ProfileScreen(mainViewModel) }

    banner().assertDoesNotExist()
  }

  @Test
  fun primeiro_acesso_nao_mostra_anuncio() {
    renderizar("Bem-vindo") { OnboardingScreen(onStart = { _, _ -> }) }

    banner().assertDoesNotExist()
  }
}

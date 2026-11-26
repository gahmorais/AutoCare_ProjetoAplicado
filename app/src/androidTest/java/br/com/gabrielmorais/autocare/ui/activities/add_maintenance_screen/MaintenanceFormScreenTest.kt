package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.gabrielmorais.autocare.data.ads.LocalAdsController
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.fakes.FakeAdsController
import br.com.gabrielmorais.autocare.fakes.FakeMaintenanceRepository
import br.com.gabrielmorais.autocare.fakes.FakeServicesRepository
import br.com.gabrielmorais.autocare.fakes.FakeVehicleRepository
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

/**
 * Substitui o AddMaintenanceActivityTest, que lancava uma Activity que deixou de
 * existir na migracao para o NavHost.
 *
 * A regressao guardada e a mesma: `return@Column` dentro da lambda do Column
 * deixava grupos abertos no Composer sem o endNode correspondente, e a
 * composicao morria com IndexOutOfBoundsException em ComposerImpl.endNode. Como
 * agora da para injetar os repositorios, os tres ramos do `when` sao exercitados
 * - antes so o de carregamento era.
 */
@RunWith(AndroidJUnit4::class)
class MaintenanceFormScreenTest {

  @get:Rule
  val compose = createComposeRule()

  private fun viewModelCom(
    services: List<Service>? = emptyList(),
    error: Throwable? = null
  ) = AddMaintenanceViewModel(
    servicesRepository = FakeServicesRepository(services, error),
    maintenanceRepository = FakeMaintenanceRepository(),
    vehicleRepository = FakeVehicleRepository(
      Vehicle(id = "v1", nickName = "Gol", averageDistanceTraveledPerMonth = 1_000)
    )
  )

  private val ads = FakeAdsController()

  private fun renderizar(viewModel: AddMaintenanceViewModel) {
    compose.setContent {
      AutoCareTheme {
        CompositionLocalProvider(LocalAdsController provides ads) {
          AddMaintenanceScreen(viewModel = viewModel, onBack = {})
        }
      }
    }
  }

  @Test
  fun compoe_enquanto_carrega_os_servicos() {
    // services = null nunca responde: a tela fica carregando.
    renderizar(viewModelCom(services = null))

    compose.onNodeWithText("Carregando tipos de serviço…").assertIsDisplayed()
  }

  @Test
  fun compoe_quando_a_lista_de_servicos_falha() {
    renderizar(viewModelCom(error = IOException("sem rede")))

    compose.onNodeWithText("Não foi possível carregar os tipos de serviço. Verifique sua conexão e tente novamente.")
      .assertIsDisplayed()
  }

  @Test
  fun compoe_o_formulario_quando_ha_servicos() {
    renderizar(
      viewModelCom(
        services = listOf(
          Service(name = "Troca de óleo", mileageChange = 7_000, mustBeDoneBefore = 7)
        )
      )
    )

    compose.onNodeWithText("Troca de óleo").assertIsDisplayed()
    compose.onNodeWithText("Já foi executada").assertIsDisplayed()
    compose.onNodeWithText("Salvar").assertIsDisplayed()
  }

  @Test
  fun registro_incompleto_nao_derruba_a_tela() {
    // Servico sem mileageChange: era exatamente esse tipo de registro que
    // derrubava a tela inteira antes da validacao.
    renderizar(
      viewModelCom(
        services = listOf(Service(name = "Servico quebrado"))
      )
    )

    compose.onNodeWithText("Não foi possível carregar os tipos de serviço. Verifique sua conexão e tente novamente.")
      .assertIsDisplayed()
  }

  // --- Descricao fora do catalogo (backup importado) ---

  private val importada = Maintenance(
    id = 7,
    description = "Troca de óleo",
    date = LocalDate.of(2026, 1, 10).toEpochDay(),
    currentMileage = 50_000,
    forecastNextExchangeMileage = 57_000,
    forecastNextExchangeDate = LocalDate.of(2026, 8, 10).toEpochDay()
  )

  private fun editarImportada(): FakeMaintenanceRepository {
    val repository = FakeMaintenanceRepository()
    val viewModel = AddMaintenanceViewModel(
      servicesRepository = FakeServicesRepository(
        listOf(
          Service(name = "Aditivo do radiador", mileageChange = 40_000, mustBeDoneBefore = 24),
          Service(name = "Óleo de motor", mileageChange = 10_000, mustBeDoneBefore = 12)
        )
      ),
      maintenanceRepository = repository,
      vehicleRepository = FakeVehicleRepository(
        Vehicle(
          id = "v1",
          nickName = "Gol",
          averageDistanceTraveledPerMonth = 1_000,
          maintenances = listOf(importada)
        )
      )
    )
    viewModel.getVehicle("v1")
    viewModel.startEditing(importada.id)
    renderizar(viewModel)
    return repository
  }

  @Test
  fun salvar_sem_mexer_mantem_a_descricao_fora_do_catalogo() {
    val repository = editarImportada()

    compose.onNodeWithText("Troca de óleo (fora do catálogo)").assertIsDisplayed()
    compose.onNodeWithText("Salvar").performScrollTo().performClick()
    compose.waitUntil { repository.saved.isNotEmpty() }

    val salva = repository.saved.single()
    assertEquals("Troca de óleo", salva.description)
    // O intersticial e pedido uma vez, so depois da gravacao confirmada.
    compose.waitUntil { ads.interstitialRequests > 0 }
    assertEquals(1, ads.interstitialRequests)
    assertEquals(importada.forecastNextExchangeMileage, salva.forecastNextExchangeMileage)
    assertEquals(importada.forecastNextExchangeDate, salva.forecastNextExchangeDate)
  }

  @Test
  fun escolher_um_servico_do_catalogo_recalcula_a_previsao() {
    val repository = editarImportada()

    compose.onNodeWithText("Troca de óleo (fora do catálogo)").performClick()
    compose.onNodeWithText("Óleo de motor").performClick()
    compose.onNodeWithText("Salvar").performScrollTo().performClick()
    compose.waitUntil { repository.saved.isNotEmpty() }

    val salva = repository.saved.single()
    assertEquals("Óleo de motor", salva.description)
    assertEquals(60_000, salva.forecastNextExchangeMileage)
    // max(10.000 km / 1.000 km por mes, 12 meses) = 12 meses depois da execucao.
    assertEquals(LocalDate.of(2027, 1, 10).toEpochDay(), salva.forecastNextExchangeDate)
  }

  @Test
  fun formulario_nao_mostra_banner_nem_pede_intersticial_sem_salvar() {
    editarImportada()

    compose.onNodeWithTag(FakeAdsController.BANNER_TAG).assertDoesNotExist()
    assertEquals(0, ads.interstitialRequests)
  }
}

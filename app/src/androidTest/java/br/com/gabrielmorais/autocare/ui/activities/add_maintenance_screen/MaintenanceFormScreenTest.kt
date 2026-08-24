package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.fakes.FakeMaintenanceRepository
import br.com.gabrielmorais.autocare.fakes.FakeServicesRepository
import br.com.gabrielmorais.autocare.fakes.FakeSessionRepository
import br.com.gabrielmorais.autocare.fakes.FakeVehicleRepository
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

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
    services: List<Service?>? = emptyList(),
    error: Throwable? = null
  ) = AddMaintenanceViewModel(
    servicesRepository = FakeServicesRepository(services, error),
    maintenanceRepository = FakeMaintenanceRepository(),
    vehicleRepository = FakeVehicleRepository(
      Vehicle(id = "v1", nickName = "Gol", averageDistanceTraveledPerMonth = 1_000)
    ),
    authRepository = FakeSessionRepository()
  )

  private fun renderizar(viewModel: AddMaintenanceViewModel) {
    compose.setContent {
      AutoCareTheme {
        AddMaintenanceScreen(viewModel = viewModel, onBack = {})
      }
    }
  }

  @Test
  fun compoe_enquanto_carrega_os_servicos() {
    // services = null deixa o callback sem resposta: a tela fica carregando.
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
        services = listOf(Service(name = "Servico quebrado"), null)
      )
    )

    compose.onNodeWithText("Não foi possível carregar os tipos de serviço. Verifique sua conexão e tente novamente.")
      .assertIsDisplayed()
  }
}

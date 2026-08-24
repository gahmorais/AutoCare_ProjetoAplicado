package br.com.gabrielmorais.autocare.ui.activities.main_screen

import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.fakes.FakeAuthRepository
import br.com.gabrielmorais.autocare.fakes.FakeUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainViewModelVehiclesTest {

  private val dispatcher = StandardTestDispatcher()
  private lateinit var userRepository: FakeUserRepository
  private lateinit var authRepository: FakeAuthRepository
  private lateinit var viewModel: MainViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    userRepository = FakeUserRepository()
    authRepository = FakeAuthRepository()
    viewModel = MainViewModel(authRepository, userRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // Regressao da Fase 1.3: os repositorios lancavam a excecao de dentro do
  // callback do Firebase e derrubavam o app. O erro tem que virar mensagem.
  @Test
  fun `falha ao salvar veiculo vira mensagem em vez de excecao`() = runTest(dispatcher) {
    userRepository.failureToReturn = IOException("Sem conexão")

    viewModel.saveVehicle(Vehicle(id = "v1", nickName = "Carro"))

    assertEquals("Sem conexão", viewModel.message.first())
    assertTrue(userRepository.savedVehicles.isEmpty())
  }

  @Test
  fun `falha ao excluir veiculo vira mensagem em vez de excecao`() = runTest(dispatcher) {
    userRepository.failureToReturn = IOException("Permission denied")

    viewModel.deleteVehicle("v1")

    assertEquals("Permission denied", viewModel.message.first())
  }

  // Regressao da Fase 4.3: o uid vinha de extra de Intent e ia direto para o
  // caminho consultado. Agora sai sempre da sessao autenticada.
  @Test
  fun `usa o uid da sessao autenticada ao salvar`() = runTest(dispatcher) {
    viewModel.saveVehicle(Vehicle(id = "v1"))

    assertEquals(listOf("v1"), userRepository.savedVehicles.map { it.id })
  }

  @Test
  fun `sem sessao ativa nao chama o repositorio`() = runTest(dispatcher) {
    authRepository.signOutForTest()

    viewModel.saveVehicle(Vehicle(id = "v1"))
    viewModel.deleteVehicle("v1")
    viewModel.updateUser(User(id = "outro-uid"))

    assertEquals("Sessão expirada", viewModel.message.first())
    assertTrue(userRepository.savedVehicles.isEmpty())
    assertTrue(userRepository.deletedVehicleIds.isEmpty())
    assertTrue(userRepository.updatedUsers.isEmpty())
  }

  // Mesmo que a UI mande outro id, a gravacao e forcada para o uid da sessao.
  @Test
  fun `updateUser sobrescreve o id com o da sessao`() = runTest(dispatcher) {
    viewModel.updateUser(User(id = "uid-de-outra-pessoa", name = "Fulano"))

    assertEquals("uid-teste", userRepository.updatedUsers.single().id)
  }

  @Test
  fun `observeUser propaga o usuario emitido`() = runTest(dispatcher) {
    userRepository.userFlow.value = User(id = "uid-teste", name = "Fulano")

    viewModel.observeUser()
    dispatcher.scheduler.advanceUntilIdle()

    assertEquals("Fulano", viewModel.user.first()?.name)
  }

  @Test
  fun `erro no fluxo de usuario vira mensagem`() = runTest(dispatcher) {
    userRepository.failureToReturn = IOException("Falha na leitura")

    viewModel.observeUser()
    dispatcher.scheduler.advanceUntilIdle()

    assertEquals("Falha na leitura", viewModel.message.first())
  }
}

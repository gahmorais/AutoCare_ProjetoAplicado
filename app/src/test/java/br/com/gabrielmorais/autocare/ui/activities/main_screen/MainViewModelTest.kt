package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.User
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
import org.mockito.Mockito
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class MainViewModelTest {

  private val dispatcher = StandardTestDispatcher()
  private lateinit var userRepository: FakeUserRepository
  private lateinit var authRepository: FakeAuthRepository
  private lateinit var viewModel: MainViewModel

  // Os fakes nunca leem o conteudo: basta uma referencia para atravessar a chamada.
  private val anyImage: Uri = Mockito.mock(Uri::class.java)

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

  @Test
  fun `foto enviada e gravada no perfil com a url retornada`() = runTest(dispatcher) {
    userRepository.userFlow.value = User(id = "uid-teste", name = "Fulano")
    viewModel.observeUser()
    dispatcher.scheduler.advanceUntilIdle()

    viewModel.updateUserPhoto(anyImage)
    dispatcher.scheduler.advanceUntilIdle()

    assertEquals(
      "https://res.cloudinary.com/teste/image/upload/v1/foto.jpg",
      userRepository.updatedUsers.single().photo
    )
  }

  // Regressao: saveUserPhoto suspende e propaga a excecao do upload. Sem o
  // runCatching na ViewModel ela escapa do viewModelScope e derruba o app.
  @Test
  fun `falha no upload vira mensagem e nao grava nada`() = runTest(dispatcher) {
    userRepository.userFlow.value = User(id = "uid-teste")
    viewModel.observeUser()
    dispatcher.scheduler.advanceUntilIdle()
    userRepository.failureToReturn = IOException("Falha no envio da imagem")

    viewModel.updateUserPhoto(anyImage)
    dispatcher.scheduler.advanceUntilIdle()

    assertEquals("Falha no envio da imagem", viewModel.message.first())
    assertTrue(userRepository.updatedUsers.isEmpty())
  }

  @Test
  fun `logout delega ao repositorio de autenticacao`() {
    viewModel.logout()

    assertTrue(authRepository.loggedOut)
  }
}

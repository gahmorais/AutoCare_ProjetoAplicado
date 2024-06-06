package br.com.gabrielmorais.autocare.ui.activities.login_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repositories.Status
import br.com.gabrielmorais.autocare.data.repositories.authorization.IAuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(private val authRepository: IAuthRepository) : ViewModel() {
  private val _loginState = Channel<LoginState<String?>>()
  val loginState = _loginState.receiveAsFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  var currentUser = _currentUser.asStateFlow()

  val loginUiState = MutableStateFlow(LoginUiState())

  init {
    getCurrentUserListener()
  }

  fun loginUser(nickname: String, password: String) {
    authRepository.login(nickname, password).onEach { resource ->
      when (resource.status) {
        Status.SUCCESS -> {
          _loginState.send(
            LoginState(
              isSuccess = "Login Realizado",
              data = resource.data?.id
            )
          )
          getCurrentUserListener()
        }

        Status.LOADING -> _loginState.send(LoginState(isLoading = true))
        Status.ERROR -> _loginState.send(LoginState(isError = resource.message ?: ""))
      }

    }.launchIn(viewModelScope)
  }


  private fun getCurrentUserListener() {
    val user = authRepository.getCurrentUser()
    if (user != null) _currentUser.update { user }
  }
}
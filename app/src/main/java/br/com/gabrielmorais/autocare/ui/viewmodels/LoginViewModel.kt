package br.com.gabrielmorais.autocare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.ui.activities.login_screen.LoginState
import br.com.gabrielmorais.autocare.ui.authorization.AuthRepository
import br.com.gabrielmorais.autocare.ui.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
  private val _loginState = Channel<LoginState>()
  val loginState = _loginState.receiveAsFlow()
  val currentUser = authRepository.currentUser
  fun loginUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
    authRepository.login(email, password).collect { result ->
      when (result) {
        is Resource.Success -> {
          _loginState.send(LoginState(isSuccess = "Login realizado com sucesso"))
        }
        is Resource.Loading -> _loginState.send(LoginState(isLoading = true))
        is Resource.Error -> _loginState.send(LoginState(isError = result.message))
      }
    }
  }
}
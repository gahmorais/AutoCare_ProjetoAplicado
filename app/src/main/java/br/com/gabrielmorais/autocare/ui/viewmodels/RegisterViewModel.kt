package br.com.gabrielmorais.autocare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterState
import br.com.gabrielmorais.autocare.ui.authorization.AuthRepository
import br.com.gabrielmorais.autocare.ui.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {
  private val _registerState = Channel<RegisterState>()
  val registerState = _registerState.receiveAsFlow()

  fun registerUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
    authRepository.register(email, password).collect { result ->
      when (result) {
        is Resource.Success -> {
          _registerState.send(RegisterState(isSuccess = "Usuário cadastrado com sucesso"))
        }
        is Resource.Loading -> {
          _registerState.send(RegisterState(isLoading = true))
        }
        is Resource.Error -> {
          _registerState.send(RegisterState(isError = result.message))
        }
      }
    }
  }
}
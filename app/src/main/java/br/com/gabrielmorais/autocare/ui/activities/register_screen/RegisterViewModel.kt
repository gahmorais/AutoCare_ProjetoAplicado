package br.com.gabrielmorais.autocare.ui.activities.register_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.Status
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
  private val authRepository: AuthRepositoryImpl,
  private val userRepository: UserRepositoryImpl
) : ViewModel() {
  private val _registerState = Channel<RegisterState>()
  val registerState = _registerState.receiveAsFlow()

  fun registerUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
    authRepository.register(email, password).collect { resource ->
      when (resource.status) {
        Status.SUCCESS -> {
          resource.data?.user?.uid?.apply {
            createUser(User(id = this, email = email))
          }
        }

        Status.LOADING -> {
          _registerState.send(RegisterState(isLoading = true))
        }

        Status.ERROR -> {
          _registerState.send(RegisterState(isError = resource.message))
        }
      }
    }
  }

  private fun createUser(user: User) {
    try {
      userRepository.createUser(user) {
        viewModelScope.launch(Dispatchers.IO) {
          _registerState.send(RegisterState(isSuccess = "Usuário cadastrado com sucesso"))
        }
      }
    } catch (e: Exception) {
      viewModelScope.launch(Dispatchers.IO) { _registerState.send(RegisterState(isError = e.message)) }
    }
  }
}
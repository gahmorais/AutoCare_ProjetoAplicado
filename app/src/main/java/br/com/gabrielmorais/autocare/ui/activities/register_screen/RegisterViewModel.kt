package br.com.gabrielmorais.autocare.ui.activities.register_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.Status
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.utils.CredentialValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository
) : ViewModel() {
  private val _registerState = Channel<RegisterState>()
  val registerState = _registerState.receiveAsFlow()

  fun registerUser(email: String, password: String, confirmPassword: String) {
    val validationError = CredentialValidator.validateRegistration(
      email = email,
      password = password,
      confirmPassword = confirmPassword
    )
    if (validationError != null) {
      viewModelScope.launch { _registerState.send(RegisterState(isError = validationError)) }
      return
    }

    viewModelScope.launch(Dispatchers.IO) {
      authRepository.register(email, password).collect { resource ->
        when (resource.status) {
          Status.SUCCESS -> {
            val uid = resource.data?.user?.uid
            if (uid == null) {
              _registerState.send(RegisterState(isError = "Não foi possível criar a conta"))
            } else {
              createUser(User(id = uid, email = email))
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
  }

  private fun createUser(user: User) {
    userRepository.createUser(
      user = user,
      callback = {
        viewModelScope.launch {
          _registerState.send(RegisterState(isSuccess = "Usuário cadastrado com sucesso"))
        }
      },
      onError = { error ->
        viewModelScope.launch {
          _registerState.send(RegisterState(isError = error.message ?: "Ocorreu um erro inesperado"))
        }
      }
    )
  }
}

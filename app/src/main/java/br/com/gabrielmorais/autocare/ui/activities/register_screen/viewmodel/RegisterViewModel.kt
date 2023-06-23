package br.com.gabrielmorais.autocare.ui.activities.register_screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.UserRepository
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterState
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.utils.Resource
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

  fun registerUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
    authRepository.register(email, password).collect { result ->
      when (result) {
        is Resource.Success -> {
          result.data?.user?.uid?.apply {
            createUser(User(id = this, email = email))
          }
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

  private fun createUser(user: User) {
    userRepository.createUser(user) { task ->
      if (task.isSuccessful) {
        viewModelScope.launch(Dispatchers.IO) {
          _registerState.send(RegisterState(isSuccess = "Usuário cadastrado com sucesso"))
        }
      } else {
        try {
          task.exception?.let { throw it }
        } catch (e: Exception) {
          viewModelScope.launch(Dispatchers.IO) { _registerState.send(RegisterState(isError = e.message)) }
        }
      }
    }
  }
}
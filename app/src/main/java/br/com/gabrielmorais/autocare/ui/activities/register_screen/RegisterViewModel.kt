package br.com.gabrielmorais.autocare.ui.activities.register_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.Status
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {
  private val _registerState = Channel<RegisterState<User>>()
  val registerState = _registerState.receiveAsFlow()

  fun registerUser(user: User) = viewModelScope.launch(Dispatchers.IO) {
    userRepository.create(user).collect { resource ->
      Timber.tag("RegisterViewModel").i("Resource: $resource")
      when (resource.status) {
        Status.SUCCESS -> {
          Timber.tag("RegisterViewModel").i("Usuário: ${resource.data}")
          _registerState.send(
            RegisterState(
              resource.status,
              message = "Usuário criado",
              data = resource.data
            )
          )
        }

        Status.LOADING -> _registerState.send(
          RegisterState(
            status = resource.status,
            message = null,
          )
        )

        Status.ERROR -> _registerState.send(
          RegisterState(
            status = resource.status,
            message = resource.message ?: "Ocorreu um erro",
          )
        )
      }
    }
  }
}
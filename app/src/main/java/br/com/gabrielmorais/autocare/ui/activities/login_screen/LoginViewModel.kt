package br.com.gabrielmorais.autocare.ui.activities.login_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.Status
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.authorization.IAuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
  private val _loginState = Channel<LoginState<String?>>()
  val loginState = _loginState.receiveAsFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  var currentUser = _currentUser.asStateFlow()

  val loginUiState = MutableStateFlow(LoginUiState())

  init {
    viewModelScope.launch { getCurrentUserListener() }
  }

  suspend fun loginUser(email: String, password: String) {
    authRepository.login(email, password).collect { resource ->
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
    }
  }


  private suspend fun getCurrentUserListener() {
    val user = authRepository.getCurrentUser()
    if (user != null) _currentUser.emit(user)
  }
}
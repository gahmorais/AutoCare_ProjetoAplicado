package br.com.gabrielmorais.autocare.ui.activities.login_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.repository.Status
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
  private val _loginState = Channel<LoginState<String?>>()
  val loginState = _loginState.receiveAsFlow()
  private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
  var currentUser = _currentUser.asStateFlow()
  val loginUiState = LoginUiState()
  init {
    getCurrentUserListener()
  }

  fun loginUser(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
    authRepository.login(email, password).collect { resource ->
      when (resource.status) {
        Status.SUCCESS -> _loginState.send(
          LoginState(
            isSuccess = "Login realizado com sucesso",
            data = resource.data?.user?.uid
          )
        )

        Status.LOADING -> _loginState.send(LoginState(isLoading = true))
        Status.ERROR -> _loginState.send(LoginState(isError = resource.message))
      }
    }
  }

  private fun getCurrentUserListener() {
    authRepository.getCurrentUserListener {
      viewModelScope.launch { _currentUser.emit(it.currentUser) }
    }
  }

}
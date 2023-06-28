package br.com.gabrielmorais.autocare.ui.activities.login_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.gabrielmorais.autocare.ui.components.PasswordTextFieldState

class LoginUiState {
  var email by mutableStateOf("")
    private set
  var passwordState = PasswordTextFieldState()
    private set
  val onEmailChange: (String) -> Unit = {
    email = it
  }
}
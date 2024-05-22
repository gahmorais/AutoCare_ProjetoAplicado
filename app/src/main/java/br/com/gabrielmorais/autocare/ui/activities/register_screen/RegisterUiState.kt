package br.com.gabrielmorais.autocare.ui.activities.register_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class RegisterUiState {
  var password by mutableStateOf("")
    private set

  var name by mutableStateOf("")
    private set

  var confirmPassword by mutableStateOf("")
    private set

  var nickname by mutableStateOf("")
    private set

  val onNameChange: (String) -> Unit = {
    name = it
  }

  val onNickNameChange: (String) -> Unit = {
    nickname = it
  }

  val onPasswordChange: (String) -> Unit = {
    password = it
  }

  val onConfirmPasswordChange: (String) -> Unit = {
    confirmPassword = it
  }

  fun allFieldsAreValid(): Pair<Boolean, String> {
    if (nickname.isEmpty()) return Pair(false, "Preencha o nickname")
    if (nickname.split(" ").size > 1) return Pair(false, "O Nickname deve conter uma única palavra")
    if (name.isEmpty()) return Pair(false, "Preencha o nome")
    if (password.isEmpty() || confirmPassword.isEmpty()) return Pair(false, "Preencha a senha")
    if (password != confirmPassword) return Pair(false, "As senha devem ser iguais")
    return Pair(true, "")
  }

}
package br.com.gabrielmorais.autocare.ui.activities.login_screen

data class LoginState(
  val isLoading: Boolean = false,
  val isSuccess: String? = "",
  val isError: String? = ""
)
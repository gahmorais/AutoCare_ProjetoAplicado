package br.com.gabrielmorais.autocare.ui.activities.login_screen

data class LoginState<out T>(
  val isLoading: Boolean = false,
  val isSuccess: String? = "",
  val isError: String? = "",
  val data: T? = null
)
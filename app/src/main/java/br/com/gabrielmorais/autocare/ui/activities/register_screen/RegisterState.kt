package br.com.gabrielmorais.autocare.ui.activities.register_screen

data class RegisterState<out T>(
  val isLoading: Boolean = false,
  val isSuccess: String = "",
  val isError: String = "",
  val data: T? = null
)
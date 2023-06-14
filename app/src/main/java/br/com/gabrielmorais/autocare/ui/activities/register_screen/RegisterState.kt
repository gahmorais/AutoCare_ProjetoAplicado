package br.com.gabrielmorais.autocare.ui.activities.register_screen

data class RegisterState(
  val isLoading: Boolean = false,
  val isSuccess: String? = "",
  val isError: String? = ""
)
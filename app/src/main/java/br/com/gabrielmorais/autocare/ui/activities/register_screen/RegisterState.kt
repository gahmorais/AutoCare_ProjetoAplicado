package br.com.gabrielmorais.autocare.ui.activities.register_screen

import br.com.gabrielmorais.autocare.data.repositories.Status

data class RegisterState<out T>(
  val status: Status,
  val message: String?,
  val data: T? = null
)
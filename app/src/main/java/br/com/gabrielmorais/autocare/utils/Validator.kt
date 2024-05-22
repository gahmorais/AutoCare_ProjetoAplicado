package br.com.gabrielmorais.autocare.utils

object Validator {
  fun isValidUsername(data: String): Boolean {
    val regex = "^[a-zA-Z0-9]{9,}$".toRegex()
    return data.matches(regex)
  }
}
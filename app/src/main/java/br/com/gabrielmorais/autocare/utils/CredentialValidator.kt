package br.com.gabrielmorais.autocare.utils

/**
 * Validacao de credenciais sem dependencia do framework Android, para poder ser
 * coberta por teste unitario puro (sem Robolectric).
 */
object CredentialValidator {

  /** Minimo aceito pelo Firebase Authentication. */
  const val MIN_PASSWORD_LENGTH = 6

  private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

  /** @return a mensagem de erro, ou null se as credenciais forem validas. */
  fun validateLogin(email: String, password: String): String? = when {
    email.isBlank() -> "Informe o e-mail"
    !EMAIL_REGEX.matches(email.trim()) -> "E-mail inválido"
    password.isEmpty() -> "Informe a senha"
    else -> null
  }

  /** @return a mensagem de erro, ou null se os dados de cadastro forem validos. */
  fun validateRegistration(email: String, password: String, confirmPassword: String): String? =
    when {
      email.isBlank() -> "Informe o e-mail"
      !EMAIL_REGEX.matches(email.trim()) -> "E-mail inválido"
      password.length < MIN_PASSWORD_LENGTH ->
        "A senha deve ter no mínimo $MIN_PASSWORD_LENGTH caracteres"

      password != confirmPassword -> "As senhas não conferem"
      else -> null
    }
}

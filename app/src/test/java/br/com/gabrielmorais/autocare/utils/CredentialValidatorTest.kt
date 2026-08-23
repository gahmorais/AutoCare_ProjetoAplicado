package br.com.gabrielmorais.autocare.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

internal class CredentialValidatorTest {

  @Test
  fun `aceita cadastro valido`() {
    assertNull(
      CredentialValidator.validateRegistration(
        email = "usuario@exemplo.com",
        password = "senha123",
        confirmPassword = "senha123"
      )
    )
  }

  // O bug original: confirmPassword era coletado na tela mas nunca comparado,
  // entao dava para se cadastrar com uma senha diferente da confirmada.
  @Test
  fun `rejeita cadastro quando a confirmacao nao confere`() {
    assertEquals(
      "As senhas não conferem",
      CredentialValidator.validateRegistration(
        email = "usuario@exemplo.com",
        password = "senha123",
        confirmPassword = "senha124"
      )
    )
  }

  @Test
  fun `rejeita senha menor que o minimo do firebase`() {
    assertNotNull(
      CredentialValidator.validateRegistration(
        email = "usuario@exemplo.com",
        password = "12345",
        confirmPassword = "12345"
      )
    )
  }

  @Test
  fun `rejeita email sem dominio`() {
    assertEquals(
      "E-mail inválido",
      CredentialValidator.validateRegistration(
        email = "usuario@exemplo",
        password = "senha123",
        confirmPassword = "senha123"
      )
    )
  }

  @Test
  fun `rejeita email em branco`() {
    assertEquals(
      "Informe o e-mail",
      CredentialValidator.validateRegistration("   ", "senha123", "senha123")
    )
  }

  @Test
  fun `aceita login valido`() {
    assertNull(CredentialValidator.validateLogin("usuario@exemplo.com", "senha123"))
  }

  @Test
  fun `rejeita login com campos vazios`() {
    assertEquals("Informe o e-mail", CredentialValidator.validateLogin("", ""))
    assertEquals("Informe a senha", CredentialValidator.validateLogin("usuario@exemplo.com", ""))
  }
}

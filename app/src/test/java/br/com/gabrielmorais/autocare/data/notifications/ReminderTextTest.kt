package br.com.gabrielmorais.autocare.data.notifications

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceReminder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReminderTextTest {

  private val today = LocalDate.of(2027, 5, 19)

  private fun reminder(
    profiles: Int = 1,
    description: String? = "Óleo de motor",
    dueInDays: Long? = 5
  ) = MaintenanceReminder(
    maintenance = Maintenance(
      id = 1,
      description = description,
      forecastNextExchangeDate = dueInDays?.let { today.plusDays(it).toEpochDay() }
    ),
    vehicleName = "Gol",
    ownerName = "Ana",
    profileCount = profiles
  )

  @Test
  fun `com um perfil o dono nao aparece`() {
    assertEquals("Óleo de motor do Gol vence em 5 dias", ReminderText.of(reminder(), today))
  }

  @Test
  fun `com mais de um perfil o dono aparece`() {
    assertEquals("Óleo de motor do Gol (Ana) vence em 5 dias", ReminderText.of(reminder(profiles = 2), today))
  }

  @Test
  fun `prazo contado no dia da notificacao`() {
    assertEquals("Óleo de motor do Gol vence amanhã", ReminderText.of(reminder(dueInDays = 1), today))
    assertEquals("Óleo de motor do Gol vence hoje", ReminderText.of(reminder(dueInDays = 0), today))
    assertEquals("Óleo de motor do Gol está vencida", ReminderText.of(reminder(dueInDays = -2), today))
  }

  @Test
  fun `sem data prevista ou sem descricao o texto continua legivel`() {
    assertEquals("Óleo de motor do Gol", ReminderText.of(reminder(dueInDays = null), today))
    assertEquals("Manutenção do Gol vence em 5 dias", ReminderText.of(reminder(description = " "), today))
  }
}

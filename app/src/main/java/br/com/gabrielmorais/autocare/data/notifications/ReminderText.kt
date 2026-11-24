package br.com.gabrielmorais.autocare.data.notifications

import br.com.gabrielmorais.autocare.data.models.MaintenanceReminder
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Texto do lembrete: "Óleo de motor do Gol (Ana) vence em 5 dias".
 *
 * O dono so aparece com mais de um perfil no aparelho - com um so ele e obvio.
 * Os dias sao contados na hora de notificar e nao fixos em 5: o alarme e
 * inexato e o aparelho pode estar desligado na data, entao o aviso pode chegar
 * depois do previsto.
 */
object ReminderText {

  fun of(reminder: MaintenanceReminder, today: LocalDate = LocalDate.now()): String {
    val service = reminder.maintenance.description?.takeIf { it.isNotBlank() } ?: "Manutenção"
    val owner = if (reminder.profileCount > 1) " (${reminder.ownerName})" else ""
    val subject = "$service do ${reminder.vehicleName}$owner"
    val due = reminder.maintenance.forecastNextExchangeDate?.let(LocalDate::ofEpochDay)
      ?: return subject
    return "$subject ${whenDue(ChronoUnit.DAYS.between(today, due))}"
  }

  private fun whenDue(days: Long): String = when {
    days < 0 -> "está vencida"
    days == 0L -> "vence hoje"
    days == 1L -> "vence amanhã"
    else -> "vence em $days dias"
  }
}

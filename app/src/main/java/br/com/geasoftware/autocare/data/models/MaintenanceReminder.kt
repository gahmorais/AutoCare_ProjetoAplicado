package br.com.geasoftware.autocare.data.models

/**
 * O que a notificacao de lembrete precisa, lido do banco na hora em que o
 * alarme dispara - e nao gravado no alarme ao agendar, senao um veiculo
 * renomeado ou transferido de perfil avisaria com o nome antigo.
 */
data class MaintenanceReminder(
  val maintenance: Maintenance,
  val vehicleName: String,
  val ownerName: String,
  /** Com um perfil so, o dono e obvio e nao vai no texto. */
  val profileCount: Int
)

package br.com.geasoftware.autocare.ui.activities.home_screen

import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.MaintenanceProgress
import br.com.geasoftware.autocare.data.models.MaintenanceStatus
import br.com.geasoftware.autocare.data.models.Vehicle
import br.com.geasoftware.autocare.data.models.displayName
import br.com.geasoftware.autocare.data.models.progress
import java.time.LocalDate

data class VehicleMaintenance(
  val vehicleId: String,
  /** Perfil dono do veiculo: tocar no card torna esse perfil o ativo. */
  val ownerId: String?,
  /** Nome do veiculo e, com mais de um perfil, o do dono ("Gol · Ana"). */
  val vehicleLabel: String,
  val maintenance: Maintenance,
  val progress: MaintenanceProgress,
  /** Vai junto porque o card refaz o calculo para desenhar a regua. */
  val averageDistancePerMonth: Int?
)

/** O que a tela inicial precisa saber, ja resolvido. */
data class HomeSummary(
  /** Vencidas e a vencer, da mais urgente para a menos. */
  val needsAttention: List<VehicleMaintenance>,
  val onTrackCount: Int,
  /**
   * Total cadastrado, concluidas inclusive. Distingue "nao ha o que cobrar"
   * de "nao ha nada cadastrado" - sem isso a tela dizia "tudo em dia" para
   * quem acabou de cadastrar o primeiro carro e nao tem manutencao nenhuma.
   */
  val totalCount: Int
)

private fun MaintenanceStatus.severity(): Int = when (this) {
  MaintenanceStatus.VENCIDA -> 0
  MaintenanceStatus.VENCE_EM_BREVE -> 1
  MaintenanceStatus.EM_DIA -> 2
  MaintenanceStatus.CONCLUIDA -> 3
}

/**
 * Agrega as manutencoes de todos os veiculos por urgencia. Antes essa pergunta
 * so era respondida entrando veiculo por veiculo - a tela inicial listava
 * carros e nao dizia nada sobre o que estava vencendo.
 */
fun List<Vehicle>.homeSummary(
  today: LocalDate = LocalDate.now(),
  /**
   * Nome de cada perfil por id. Vazio com um perfil so: ai o dono e obvio e o
   * nome so ocuparia espaco no card.
   */
  ownerNames: Map<String, String> = emptyMap()
): HomeSummary {
  val todas = flatMap { vehicle ->
    val vehicleId = vehicle.id ?: return@flatMap emptyList()
    val owner = vehicle.profileId?.let(ownerNames::get)
    val label = if (owner != null) "${vehicle.displayName()} · $owner" else vehicle.displayName()
    vehicle.maintenances.orEmpty().map { maintenance ->
      VehicleMaintenance(
        vehicleId = vehicleId,
        ownerId = vehicle.profileId,
        vehicleLabel = label,
        maintenance = maintenance,
        progress = maintenance.progress(vehicle.averageDistanceTraveledPerMonth, today),
        averageDistancePerMonth = vehicle.averageDistanceTraveledPerMonth
      )
    }
  }

  val pendentes = todas
    .filter {
      it.progress.status == MaintenanceStatus.VENCIDA ||
        it.progress.status == MaintenanceStatus.VENCE_EM_BREVE
    }
    // Vencidas primeiro; dentro do mesmo status, a que avancou mais no intervalo.
    .sortedWith(
      compareBy<VehicleMaintenance> { it.progress.status.severity() }
        .thenByDescending { it.progress.fraction }
    )

  return HomeSummary(
    needsAttention = pendentes,
    onTrackCount = todas.count { it.progress.status == MaintenanceStatus.EM_DIA },
    totalCount = todas.size
  )
}

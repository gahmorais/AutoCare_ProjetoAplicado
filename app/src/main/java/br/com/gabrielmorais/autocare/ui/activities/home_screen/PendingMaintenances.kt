package br.com.gabrielmorais.autocare.ui.activities.home_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceProgress
import br.com.gabrielmorais.autocare.data.models.MaintenanceStatus
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.models.progress
import java.time.LocalDate

data class VehicleMaintenance(
  val vehicleId: String,
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

/**
 * Apelido, ou marca e modelo, ou placa - o primeiro que existir. Um veiculo
 * cadastrado so com placa nao pode virar uma linha em branco na lista.
 */
internal fun Vehicle.label(): String =
  nickName?.takeIf { it.isNotBlank() }
    ?: listOfNotNull(brand?.takeIf { it.isNotBlank() }, model?.takeIf { it.isNotBlank() })
      .takeIf { it.isNotEmpty() }
      ?.joinToString(" ")
    ?: plate?.takeIf { it.isNotBlank() }
    ?: "Veículo sem nome"

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
fun List<Vehicle>.homeSummary(today: LocalDate = LocalDate.now()): HomeSummary {
  val todas = flatMap { vehicle ->
    val vehicleId = vehicle.id ?: return@flatMap emptyList()
    val label = vehicle.label()
    vehicle.maintenances.orEmpty().map { maintenance ->
      VehicleMaintenance(
        vehicleId = vehicleId,
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

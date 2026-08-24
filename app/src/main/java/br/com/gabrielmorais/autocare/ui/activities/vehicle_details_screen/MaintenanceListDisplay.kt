package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance

enum class MaintenanceFilter {
  TODAS,
  PENDENTES,
  CONCLUIDAS
}

fun List<Maintenance>.filteredBy(filter: MaintenanceFilter): List<Maintenance> = when (filter) {
  MaintenanceFilter.TODAS -> this
  MaintenanceFilter.PENDENTES -> filterNot { it.completed }
  MaintenanceFilter.CONCLUIDAS -> filter { it.completed }
}

/**
 * Pendentes primeiro, concluidas no fim. O criterio de data muda por grupo
 * porque a data relevante e outra: numa pendente importa quando vence a
 * proxima troca (a mais proxima no topo); numa concluida, quando foi executada
 * (a mais recente no topo). Datas nulas - possiveis em registros antigos - vao
 * para o fim do proprio grupo.
 */
fun List<Maintenance>.sortedForDisplay(): List<Maintenance> {
  val (pendentes, concluidas) = partition { !it.completed }
  return pendentes.sortedBy { it.forecastNextExchangeDate ?: Long.MAX_VALUE } +
    concluidas.sortedByDescending { it.date ?: Long.MIN_VALUE }
}

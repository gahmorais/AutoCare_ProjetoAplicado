package br.com.gabrielmorais.autocare.ui.activities.home_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceStatus
import br.com.gabrielmorais.autocare.data.models.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class PendingMaintenancesTest {

  private val feitoEm = LocalDate.of(2025, Month.JANUARY, 1)

  private fun manutencao(
    id: Int,
    descricao: String,
    forecastDate: LocalDate,
    completed: Boolean = false
  ) = Maintenance(
    id = id,
    description = descricao,
    date = feitoEm.toEpochDay(),
    currentMileage = 75_000,
    forecastNextExchangeMileage = 82_000,
    forecastNextExchangeDate = forecastDate.toEpochDay(),
    completed = completed
  )

  private fun veiculo(id: String, apelido: String?, vararg manutencoes: Maintenance) = Vehicle(
    id = id,
    nickName = apelido,
    brand = "Volkswagen",
    model = "Gol",
    plate = "ABC1D23",
    averageDistanceTraveledPerMonth = 1_000,
    maintenances = manutencoes.toList()
  )

  @Test
  fun `junta manutencoes de veiculos diferentes numa lista so`() {
    val hoje = feitoEm.plusMonths(8)
    val frota = listOf(
      veiculo("v1", "Gol", manutencao(1, "Óleo", feitoEm.plusMonths(7))),
      veiculo("v2", "Onix", manutencao(2, "Alinhamento", feitoEm.plusMonths(7)))
    )

    val resumo = frota.homeSummary(hoje)

    assertEquals(2, resumo.needsAttention.size)
    assertEquals(setOf("Gol", "Onix"), resumo.needsAttention.map { it.vehicleLabel }.toSet())
  }

  @Test
  fun `vencidas vem antes das que vencem em breve`() {
    val hoje = feitoEm.plusMonths(7)
    val frota = listOf(
      veiculo(
        "v1", "Gol",
        // vence daqui a pouco
        manutencao(1, "Em breve", feitoEm.plusMonths(7).plusDays(3)),
        // ja passou
        manutencao(2, "Vencida", feitoEm.plusMonths(6))
      )
    )

    val resumo = frota.homeSummary(hoje)

    assertEquals("Vencida", resumo.needsAttention.first().maintenance.description)
    assertEquals(MaintenanceStatus.VENCIDA, resumo.needsAttention.first().progress.status)
  }

  @Test
  fun `concluidas e em dia ficam fora da lista de atencao`() {
    val hoje = feitoEm.plusMonths(1)
    val frota = listOf(
      veiculo(
        "v1", "Gol",
        manutencao(1, "Em dia", feitoEm.plusMonths(7)),
        manutencao(2, "Concluída", feitoEm.plusMonths(2), completed = true)
      )
    )

    val resumo = frota.homeSummary(hoje)

    assertTrue(resumo.needsAttention.isEmpty())
    assertEquals(1, resumo.onTrackCount)
  }

  @Test
  fun `veiculo sem id nao entra na lista`() {
    val semId = Vehicle(id = null, maintenances = listOf(manutencao(1, "Óleo", feitoEm)))

    assertTrue(listOf(semId).homeSummary(feitoEm.plusMonths(8)).needsAttention.isEmpty())
  }

  @Test
  fun `rotulo cai para marca e modelo quando nao ha apelido`() {
    val frota = listOf(veiculo("v1", null, manutencao(1, "Óleo", feitoEm.plusMonths(6))))

    val resumo = frota.homeSummary(feitoEm.plusMonths(8))

    assertEquals("Volkswagen Gol", resumo.needsAttention.single().vehicleLabel)
  }

  @Test
  fun `rotulo cai para a placa quando nao ha apelido nem marca`() {
    val soPlaca = Vehicle(
      id = "v1",
      plate = "XYZ9F87",
      averageDistanceTraveledPerMonth = 1_000,
      maintenances = listOf(manutencao(1, "Óleo", feitoEm.plusMonths(6)))
    )

    val resumo = listOf(soPlaca).homeSummary(feitoEm.plusMonths(8))

    assertEquals("XYZ9F87", resumo.needsAttention.single().vehicleLabel)
  }

  @Test
  fun `frota vazia devolve resumo vazio`() {
    val resumo = emptyList<Vehicle>().homeSummary(feitoEm)

    assertTrue(resumo.needsAttention.isEmpty())
    assertEquals(0, resumo.onTrackCount)
    assertEquals(0, resumo.totalCount)
  }

  @Test
  fun `veiculo sem manutencao nenhuma nao conta como em dia`() {
    // A tela usava onTrackCount para decidir entre "tudo em dia" e a lista, e
    // quem acabou de cadastrar o primeiro carro via "0 servico em dia".
    val semManutencoes = Vehicle(id = "v1", nickName = "Gol", maintenances = null)

    val resumo = listOf(semManutencoes).homeSummary(feitoEm)

    assertEquals(0, resumo.totalCount)
    assertEquals(0, resumo.onTrackCount)
    assertTrue(resumo.needsAttention.isEmpty())
  }

  @Test
  fun `total conta tambem as concluidas`() {
    val hoje = feitoEm.plusMonths(1)
    val frota = listOf(
      veiculo(
        "v1", "Gol",
        manutencao(1, "Em dia", feitoEm.plusMonths(7)),
        manutencao(2, "Concluída", feitoEm.plusMonths(2), completed = true)
      )
    )

    assertEquals(2, frota.homeSummary(hoje).totalCount)
  }
}

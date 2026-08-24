package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class MaintenanceListDisplayTest {

  private fun epochDay(year: Int, month: Month, day: Int) =
    LocalDate.of(year, month, day).toEpochDay()

  private val pendenteProxima = Maintenance(
    id = 1,
    forecastNextExchangeDate = epochDay(2026, Month.JANUARY, 10),
    completed = false
  )
  private val pendenteDistante = Maintenance(
    id = 2,
    forecastNextExchangeDate = epochDay(2026, Month.DECEMBER, 10),
    completed = false
  )
  private val pendenteSemData = Maintenance(id = 3, forecastNextExchangeDate = null)
  private val concluidaAntiga = Maintenance(
    id = 4,
    date = epochDay(2024, Month.MARCH, 1),
    completed = true
  )
  private val concluidaRecente = Maintenance(
    id = 5,
    date = epochDay(2025, Month.MARCH, 1),
    completed = true
  )
  private val concluidaSemData = Maintenance(id = 6, date = null, completed = true)

  @Test
  fun `sortedForDisplay coloca as pendentes antes das concluidas`() {
    val ordenada = listOf(concluidaRecente, pendenteProxima).sortedForDisplay()

    assertEquals(listOf(pendenteProxima, concluidaRecente), ordenada)
  }

  @Test
  fun `sortedForDisplay ordena pendentes pela previsao mais proxima`() {
    val ordenada = listOf(pendenteDistante, pendenteProxima).sortedForDisplay()

    assertEquals(listOf(pendenteProxima, pendenteDistante), ordenada)
  }

  @Test
  fun `sortedForDisplay ordena concluidas da execucao mais recente para a mais antiga`() {
    val ordenada = listOf(concluidaAntiga, concluidaRecente).sortedForDisplay()

    assertEquals(listOf(concluidaRecente, concluidaAntiga), ordenada)
  }

  @Test
  fun `sortedForDisplay joga datas nulas para o fim do proprio grupo`() {
    val ordenada = listOf(
      concluidaSemData,
      pendenteSemData,
      concluidaRecente,
      pendenteProxima
    ).sortedForDisplay()

    assertEquals(
      listOf(pendenteProxima, pendenteSemData, concluidaRecente, concluidaSemData),
      ordenada
    )
  }

  @Test
  fun `filteredBy separa pendentes e concluidas`() {
    val lista = listOf(pendenteProxima, concluidaRecente)

    assertEquals(lista, lista.filteredBy(MaintenanceFilter.TODAS))
    assertEquals(listOf(pendenteProxima), lista.filteredBy(MaintenanceFilter.PENDENTES))
    assertEquals(listOf(concluidaRecente), lista.filteredBy(MaintenanceFilter.CONCLUIDAS))
  }
}

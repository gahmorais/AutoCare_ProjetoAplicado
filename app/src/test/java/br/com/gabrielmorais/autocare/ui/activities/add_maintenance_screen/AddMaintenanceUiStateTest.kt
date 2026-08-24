package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class AddMaintenanceUiStateTest {

  private val executada = LocalDate.of(2025, Month.FEBRUARY, 25)
  private val proxima = LocalDate.of(2025, Month.SEPTEMBER, 30)

  private val maintenance = Maintenance(
    id = 7,
    description = "Troca de óleo",
    date = executada.toEpochDay(),
    currentMileage = 75000,
    forecastNextExchangeMileage = 82000,
    forecastNextExchangeDate = proxima.toEpochDay(),
    comments = "Óleo ELF 10W40",
    completed = true
  )

  @Test
  fun `sem manutencao usa os defaults de criacao`() {
    val state = AddMaintenanceUiState()

    assertEquals(LocalDate.now(), state.date)
    assertEquals("0", state.currentMileage)
    assertEquals("0", state.forecastNextExchangeMileage)
    assertEquals("", state.comments)
    assertFalse(state.completed)
  }

  @Test
  fun `semeia todos os campos a partir da manutencao existente`() {
    val state = AddMaintenanceUiState(maintenance)

    assertEquals(executada, state.date)
    assertEquals("75000", state.currentMileage)
    assertEquals("82000", state.forecastNextExchangeMileage)
    assertEquals(proxima, state.forecastNextExchangeDate)
    assertEquals("Óleo ELF 10W40", state.comments)
    assertTrue(state.completed)
  }

  @Test
  fun `campos nulos de registros antigos caem nos defaults`() {
    val state = AddMaintenanceUiState(Maintenance(id = 8))

    assertEquals(LocalDate.now(), state.date)
    assertEquals("0", state.currentMileage)
    assertEquals("", state.comments)
    assertFalse(state.completed)
  }

  @Test
  fun `onCompletedChange alterna o estado de concluida`() {
    val state = AddMaintenanceUiState()

    state.onCompletedChange(true)

    assertTrue(state.completed)
  }
}

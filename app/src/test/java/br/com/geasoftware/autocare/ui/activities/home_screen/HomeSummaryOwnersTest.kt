package br.com.geasoftware.autocare.ui.activities.home_screen

import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Vehicle
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HomeSummaryOwnersTest {

  private val today = LocalDate.of(2027, 1, 10)

  // Vencida por data: o card entra em "precisa de atencao".
  private fun overdue(id: Int) = Maintenance(
    id = id,
    date = today.minusMonths(6).toEpochDay(),
    forecastNextExchangeDate = today.minusDays(1).toEpochDay()
  )

  private val vehicles = listOf(
    Vehicle(id = "gol", nickName = "Gol", profileId = "ana", maintenances = listOf(overdue(1))),
    Vehicle(id = "uno", nickName = "Uno", profileId = "pedro", maintenances = listOf(overdue(2)))
  )

  @Test
  fun `sem nomes de dono o card mostra so o veiculo`() {
    val labels = vehicles.homeSummary(today).needsAttention.map { it.vehicleLabel }.toSet()
    assertEquals(setOf("Gol", "Uno"), labels)
  }

  @Test
  fun `com nomes de dono o card mostra veiculo e dono`() {
    val summary = vehicles.homeSummary(today, ownerNames = mapOf("ana" to "Ana", "pedro" to "Pedro"))
    assertEquals(setOf("Gol · Ana", "Uno · Pedro"), summary.needsAttention.map { it.vehicleLabel }.toSet())
  }

  @Test
  fun `cada pendencia sabe o perfil dono`() {
    val owners = vehicles.homeSummary(today).needsAttention.associate { it.vehicleId to it.ownerId }
    assertEquals(mapOf("gol" to "ana", "uno" to "pedro"), owners)
  }
}

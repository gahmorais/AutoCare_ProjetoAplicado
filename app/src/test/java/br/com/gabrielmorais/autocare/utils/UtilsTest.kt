package br.com.gabrielmorais.autocare.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

internal class UtilsTest {

  @Test
  fun `soma a quantidade de meses informada a data`() {
    val currentDate = LocalDate.of(2023, 7, 9)
    assertEquals(LocalDate.of(2024, 2, 9), Utils.futureDateMonth(currentDate, 7))
  }

  @Test
  fun `formata a data no padrao brasileiro`() {
    val currentDate = LocalDate.of(2023, 7, 9)
    assertEquals("09/07/2023", Utils.formatDate(currentDate.toEpochDay()))
  }

  @Test
  fun `calcula os meses ate a proxima manutencao`() {
    assertEquals(120, Utils.calculateNextMaintenanceInMonths(500, 60000))
  }

  // Antes o `/` estourava ArithmeticException e derrubava a tela de manutencao
  // quando o veiculo tinha media zero.
  @Test
  fun `retorna null quando a media mensal e zero`() {
    assertNull(Utils.calculateNextMaintenanceInMonths(0, 60000))
  }

  @Test
  fun `retorna null quando a media mensal e negativa`() {
    assertNull(Utils.calculateNextMaintenanceInMonths(-100, 60000))
  }

  @Test
  fun `o lembrete cai cinco dias antes as sete da manha`() {
    val maintenanceDate = LocalDate.of(2023, 7, 10)
    val reminder = Utils.dateMinusFiveDays(maintenanceDate)

    assertEquals(LocalDate.of(2023, 7, 5), reminder.toLocalDate())
    assertEquals(7, reminder.hour)
    assertEquals(0, reminder.minute)
  }

  // Regressao: a virada de mes era o caso que o Calendar.set com mes 1-12
  // quebrava no agendamento antigo.
  @Test
  fun `o lembrete atravessa a virada de mes corretamente`() {
    val reminder = Utils.dateMinusFiveDays(LocalDate.of(2023, 3, 2))

    assertEquals(LocalDate.of(2023, 2, 25), reminder.toLocalDate())
  }
}

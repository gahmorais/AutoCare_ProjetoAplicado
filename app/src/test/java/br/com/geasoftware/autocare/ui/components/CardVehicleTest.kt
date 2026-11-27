package br.com.geasoftware.autocare.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class CardVehicleTest {

  @Test
  fun `distancia usa o separador de milhar brasileiro`() {
    assertEquals("1.500", formatMonthlyDistance(1_500))
  }

  @Test
  fun `distancia ausente vira travessao em vez de derrubar a tela`() {
    // Veiculo vindo de backup sem averageDistancePerMonth.
    assertEquals("—", formatMonthlyDistance(null))
  }
}

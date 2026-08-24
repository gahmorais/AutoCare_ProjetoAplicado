package br.com.gabrielmorais.autocare.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class MaintenanceProgressTest {

  private val feitoEm = LocalDate.of(2025, Month.JANUARY, 1)

  /** Intervalo de 7.000 km e 7 meses; a 1.000 km/mes as duas dimensoes andam juntas. */
  private fun manutencao(
    completed: Boolean = false,
    currentMileage: Int? = 75_000,
    forecastMileage: Int? = 82_000,
    forecastDate: LocalDate? = feitoEm.plusMonths(7)
  ) = Maintenance(
    id = 1,
    description = "Troca de óleo",
    date = feitoEm.toEpochDay(),
    currentMileage = currentMileage,
    forecastNextExchangeMileage = forecastMileage,
    forecastNextExchangeDate = forecastDate?.toEpochDay(),
    completed = completed
  )

  // --- estimativa de quilometragem -------------------------------------------

  @Test
  fun `estimativa soma a media mensal ao tempo decorrido`() {
    // 90 dias corridos / 30,44 dias por mes x 1.000 km = 2.956 km
    val estimada = manutencao().estimatedMileageAt(1_000, feitoEm.plusMonths(3))

    assertEquals(77_956, estimada)
  }

  @Test
  fun `estimativa no proprio dia do servico e a quilometragem registrada`() {
    assertEquals(75_000, manutencao().estimatedMileageAt(1_000, feitoEm))
  }

  @Test
  fun `sem media mensal nao ha estimativa`() {
    assertNull(manutencao().estimatedMileageAt(null, feitoEm.plusMonths(3)))
  }

  @Test
  fun `media zero nao estima em vez de dividir por zero`() {
    assertNull(manutencao().estimatedMileageAt(0, feitoEm.plusMonths(3)))
  }

  @Test
  fun `sem quilometragem registrada nao ha estimativa`() {
    assertNull(manutencao(currentMileage = null).estimatedMileageAt(1_000, feitoEm.plusMonths(3)))
  }

  // --- status ----------------------------------------------------------------

  @Test
  fun `concluida ignora data e quilometragem`() {
    val progresso = manutencao(completed = true).progress(1_000, feitoEm.plusYears(5))

    assertEquals(MaintenanceStatus.CONCLUIDA, progresso.status)
  }

  @Test
  fun `logo apos o servico esta em dia`() {
    val progresso = manutencao().progress(1_000, feitoEm.plusMonths(1))

    assertEquals(MaintenanceStatus.EM_DIA, progresso.status)
  }

  @Test
  fun `perto do fim do intervalo vence em breve`() {
    // 6,5 de 7 meses ~ 93% do intervalo
    val progresso = manutencao().progress(1_000, feitoEm.plusMonths(6).plusDays(15))

    assertEquals(MaintenanceStatus.VENCE_EM_BREVE, progresso.status)
  }

  @Test
  fun `passado o intervalo esta vencida`() {
    val progresso = manutencao().progress(1_000, feitoEm.plusMonths(8))

    assertEquals(MaintenanceStatus.VENCIDA, progresso.status)
  }

  @Test
  fun `so com a data prevista, avisa na mesma janela de cinco dias do lembrete`() {
    // Registro sem data de execucao nem km: nao produz fracao nenhuma, entao
    // quem decide e a janela de 5 dias - a mesma que agenda a notificacao.
    val soPrevisao = Maintenance(
      id = 3,
      forecastNextExchangeDate = LocalDate.of(2025, Month.MARCH, 10).toEpochDay()
    )

    val progresso = soPrevisao.progress(1_000, LocalDate.of(2025, Month.MARCH, 7))

    assertEquals(MaintenanceStatus.VENCE_EM_BREVE, progresso.status)
  }

  @Test
  fun `so com a data prevista, cobra depois que ela passa`() {
    val soPrevisao = Maintenance(
      id = 4,
      forecastNextExchangeDate = LocalDate.of(2025, Month.MARCH, 10).toEpochDay()
    )

    val progresso = soPrevisao.progress(1_000, LocalDate.of(2025, Month.MARCH, 11))

    assertEquals(MaintenanceStatus.VENCIDA, progresso.status)
  }

  @Test
  fun `estoura por quilometragem mesmo com a data em dia`() {
    // Roda 5.000 km por mes: passa dos 82.000 km muito antes dos 7 meses.
    val progresso = manutencao().progress(5_000, feitoEm.plusMonths(2))

    assertEquals(MaintenanceStatus.VENCIDA, progresso.status)
  }

  @Test
  fun `sem data e sem quilometragem nao afirma atraso`() {
    val vazia = Maintenance(id = 2)

    assertEquals(MaintenanceStatus.EM_DIA, vazia.progress(1_000, feitoEm).status)
  }

  // --- dados para a regua -----------------------------------------------------

  @Test
  fun `concluida enche a barra mesmo tendo sido feita antes do previsto`() {
    // Um mes apos o servico, a 1.000 km-mes, a fracao natural seria ~0,15.
    val progresso = manutencao(completed = true).progress(1_000, feitoEm.plusMonths(1))

    assertEquals(1f, progresso.fraction, 0.0001f)
  }

  @Test
  fun `fracao fica limitada a um mesmo passando do previsto`() {
    val progresso = manutencao().progress(1_000, feitoEm.plusYears(2))

    assertEquals(1f, progresso.fraction, 0.0001f)
  }

  @Test
  fun `excedente conta os quilometros passados do previsto`() {
    // 304 dias / 30,44 x 1.000 = 9.986 km -> 84.986, contra 82.000 previstos
    val progresso = manutencao().progress(1_000, feitoEm.plusMonths(10))

    assertEquals(2_986, progresso.overshootKm)
  }

  @Test
  fun `sem excedente enquanto nao passa do previsto`() {
    assertNull(manutencao().progress(1_000, feitoEm.plusMonths(2)).overshootKm)
  }

  @Test
  fun `a regua leva as pontas mesmo sem estimativa`() {
    val progresso = manutencao().progress(null, feitoEm.plusMonths(2))

    assertEquals(75_000, progresso.startMileage)
    assertEquals(82_000, progresso.forecastMileage)
    assertNull(progresso.estimatedMileage)
  }
}

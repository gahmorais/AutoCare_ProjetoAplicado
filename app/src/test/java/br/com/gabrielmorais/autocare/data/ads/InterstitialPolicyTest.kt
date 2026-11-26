package br.com.gabrielmorais.autocare.data.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class InterstitialPolicyTest {

  private val zone = ZoneId.of("America/Sao_Paulo")
  private var now: Instant = LocalDateTime.of(2026, 11, 26, 10, 0).atZone(zone).toInstant()
  private val policy = InterstitialPolicy(now = { now }, zone = { zone })

  /** Segunda sessao em diante, nada exibido ainda. */
  private val veterano = InterstitialHistory(sessions = 2)

  private fun avancar(duracao: Duration) {
    now = now.plus(duracao)
  }

  @Test
  fun `nunca na primeira sessao`() {
    val primeira = policy.startSession(InterstitialHistory())

    assertEquals(1, primeira.sessions)
    assertFalse(policy.canShow(primeira))
    assertTrue(policy.canShow(policy.startSession(primeira)))
  }

  @Test
  fun `respeita o intervalo minimo de 3 minutos`() {
    val exibido = policy.recordShown(veterano)

    avancar(Duration.ofMinutes(2).plusSeconds(59))
    assertFalse(policy.canShow(exibido))

    avancar(Duration.ofSeconds(1))
    assertTrue(policy.canShow(exibido))
  }

  @Test
  fun `no maximo 3 por dia`() {
    var historico = veterano
    repeat(3) {
      assertTrue(policy.canShow(historico))
      historico = policy.recordShown(historico)
      avancar(Duration.ofMinutes(10))
    }

    assertEquals(3, historico.shownOnDay)
    assertFalse(policy.canShow(historico))
  }

  @Test
  fun `a contagem diaria zera na virada do dia no fuso do aparelho`() {
    now = LocalDateTime.of(2026, 11, 26, 23, 50).atZone(zone).toInstant()
    var historico = veterano.copy(
      shownDay = now.atZone(zone).toLocalDate(),
      shownOnDay = 3,
      lastShownAt = now.minus(Duration.ofHours(1))
    )
    assertFalse(policy.canShow(historico))

    avancar(Duration.ofMinutes(15)) // 00:05 do dia 27
    assertTrue(policy.canShow(historico))

    historico = policy.recordShown(historico)
    assertEquals(1, historico.shownOnDay)
    assertEquals(now.atZone(zone).toLocalDate(), historico.shownDay)
  }
}

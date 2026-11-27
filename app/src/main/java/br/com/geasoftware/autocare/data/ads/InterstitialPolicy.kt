package br.com.geasoftware.autocare.data.ads

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * O que a [InterstitialPolicy] precisa lembrar entre aberturas do app.
 *
 * @param sessions quantas vezes o app foi aberto (processo novo), contando a atual.
 * @param shownDay dia, no fuso do aparelho, a que [shownOnDay] se refere.
 */
data class InterstitialHistory(
  val sessions: Int = 0,
  val lastShownAt: Instant? = null,
  val shownDay: LocalDate? = null,
  val shownOnDay: Int = 0
)

/**
 * Quando o intersticial pode aparecer (so depois de salvar uma manutencao).
 * Kotlin puro e com relogio injetado, para os testes cobrirem cada regra sem
 * esperar minutos nem virar o dia de verdade.
 *
 * - nunca na primeira sessao: quem acabou de instalar ainda esta conhecendo o app;
 * - no minimo [minInterval] entre duas exibicoes;
 * - no maximo [maxPerDay] por dia, com o dia contado no fuso do aparelho.
 */
class InterstitialPolicy(
  private val now: () -> Instant = Instant::now,
  private val zone: () -> ZoneId = ZoneId::systemDefault,
  val minInterval: Duration = Duration.ofMinutes(3),
  val maxPerDay: Int = 3
) {

  fun startSession(history: InterstitialHistory): InterstitialHistory =
    history.copy(sessions = history.sessions + 1)

  fun canShow(history: InterstitialHistory): Boolean {
    if (history.sessions <= 1) return false
    val last = history.lastShownAt
    if (last != null && Duration.between(last, now()) < minInterval) return false
    return shownToday(history) < maxPerDay
  }

  fun recordShown(history: InterstitialHistory): InterstitialHistory = history.copy(
    lastShownAt = now(),
    shownDay = today(),
    shownOnDay = shownToday(history) + 1
  )

  private fun today(): LocalDate = now().atZone(zone()).toLocalDate()

  private fun shownToday(history: InterstitialHistory): Int =
    if (history.shownDay == today()) history.shownOnDay else 0
}

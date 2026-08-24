package br.com.gabrielmorais.autocare.data.models

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class MaintenanceStatus {
  EM_DIA,
  VENCE_EM_BREVE,
  VENCIDA,
  CONCLUIDA
}

/**
 * Tudo que a regua de quilometragem precisa desenhar, ja resolvido.
 *
 * [estimatedMileage] e sempre estimativa: o veiculo guarda a media mensal
 * percorrida, nao a quilometragem corrente. A UI marca esse numero com "≈" para
 * nao passar por leitura de hodometro.
 */
data class MaintenanceProgress(
  val status: MaintenanceStatus,
  val startMileage: Int?,
  val forecastMileage: Int?,
  val estimatedMileage: Int?,
  /** 0f..1f, ja limitado - o marcador nunca sai da barra. */
  val fraction: Float,
  /** Quantos km passaram da previsao. Null quando nao esta vencida por km. */
  val overshootKm: Int?
)

/** Dias de antecedencia do lembrete; o card usa o mesmo numero para nao discordar dele. */
private const val DIAS_DE_AVISO = 5L

/** A partir de quanto do intervalo percorrido o servico ja conta como proximo. */
private const val FRACAO_DE_AVISO = 0.9f

private const val DIAS_POR_MES = 30.44

/**
 * Quilometragem estimada hoje: a do ultimo servico mais a media mensal aplicada
 * ao tempo decorrido. Null quando falta qualquer um dos tres dados, ou quando a
 * media e invalida - foi divisao por media zero que ja estourou neste projeto.
 */
fun Maintenance.estimatedMileageAt(
  averageDistancePerMonth: Int?,
  today: LocalDate = LocalDate.now()
): Int? {
  val start = currentMileage ?: return null
  val doneOn = date?.let { LocalDate.ofEpochDay(it) } ?: return null
  val average = averageDistancePerMonth?.takeIf { it > 0 } ?: return null

  val days = ChronoUnit.DAYS.between(doneOn, today)
  if (days <= 0) return start
  return start + (average * (days / DIAS_POR_MES)).toInt()
}

/**
 * O servico vence por data ou por quilometragem, o que chegar primeiro - entao o
 * progresso e o maior dos dois. Concluida curto-circuita tudo: nao ha o que vencer.
 */
fun Maintenance.progress(
  averageDistancePerMonth: Int?,
  today: LocalDate = LocalDate.now()
): MaintenanceProgress {
  val estimated = estimatedMileageAt(averageDistancePerMonth, today)

  val byMileage = fractionByMileage(estimated)
  val byDate = fractionByDate(today)
  val fraction = maxOf(byMileage ?: 0f, byDate ?: 0f)

  val diasRestantes = forecastNextExchangeDate
    ?.let { ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(it)) }

  val status = when {
    completed -> MaintenanceStatus.CONCLUIDA
    fraction >= 1f -> MaintenanceStatus.VENCIDA
    // A data prevista sozinha basta para cobrar: um registro antigo sem data de
    // execucao nao produz fracao nenhuma, e sem esta linha ficaria "em dia" para
    // sempre por falta de dado.
    diasRestantes != null && diasRestantes < 0 -> MaintenanceStatus.VENCIDA
    fraction >= FRACAO_DE_AVISO -> MaintenanceStatus.VENCE_EM_BREVE
    diasRestantes != null && diasRestantes <= DIAS_DE_AVISO -> MaintenanceStatus.VENCE_EM_BREVE
    // Sem nenhum dos sinais nao da para afirmar atraso: fica em dia.
    else -> MaintenanceStatus.EM_DIA
  }

  val overshoot = forecastNextExchangeMileage
    ?.let { alvo -> estimated?.minus(alvo) }
    ?.takeIf { it > 0 }

  return MaintenanceProgress(
    status = status,
    startMileage = currentMileage,
    forecastMileage = forecastNextExchangeMileage,
    estimatedMileage = estimated,
    // Concluida enche a barra por definicao, e nao por acaso: sem esta linha a
    // fracao continuava vindo das datas, e uma manutencao marcada como feita
    // antes do previsto apareceria pela metade.
    fraction = if (completed) 1f else fraction.coerceIn(0f, 1f),
    overshootKm = overshoot
  )
}

private fun Maintenance.fractionByMileage(estimated: Int?): Float? {
  val start = currentMileage ?: return null
  val target = forecastNextExchangeMileage ?: return null
  val now = estimated ?: return null
  val intervalo = target - start
  if (intervalo <= 0) return null
  return (now - start).toFloat() / intervalo
}

private fun Maintenance.fractionByDate(today: LocalDate): Float? {
  val doneOn = date?.let { LocalDate.ofEpochDay(it) } ?: return null
  val target = forecastNextExchangeDate?.let { LocalDate.ofEpochDay(it) } ?: return null
  val intervalo = ChronoUnit.DAYS.between(doneOn, target)
  if (intervalo <= 0) return null
  return ChronoUnit.DAYS.between(doneOn, today).toFloat() / intervalo
}

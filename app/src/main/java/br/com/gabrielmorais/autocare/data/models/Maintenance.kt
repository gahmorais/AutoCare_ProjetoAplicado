package br.com.gabrielmorais.autocare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
data class Maintenance(
  // SystemClock.uptimeMillis() zera a cada reboot, entao manutencoes criadas
  // apos reboots diferentes colidiam no id - e o id e usado tanto como
  // requestCode do PendingIntent quanto como id da notificacao, fazendo uma
  // sobrescrever o alarme e a notificacao da outra.
  val id: Int = Random.nextInt(),
  val description: String? = null,
  val date: Long? = null,
  val currentMileage: Int? = null,
  val forecastNextExchangeMileage: Int? = null,
  val forecastNextExchangeDate: Long? = null,
  val comments: String? = null,
) : Parcelable

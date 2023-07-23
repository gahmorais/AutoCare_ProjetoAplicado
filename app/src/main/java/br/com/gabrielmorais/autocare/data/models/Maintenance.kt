package br.com.gabrielmorais.autocare.data.models

import android.os.Parcelable
import android.os.SystemClock
import kotlinx.parcelize.Parcelize

@Parcelize
data class Maintenance(
  val id: Int = SystemClock.uptimeMillis().toInt(),
  val description: String? = null,
  val date: Long? = null,
  val currentMileage: Int? = null,
  val forecastNextExchangeMileage: Int? = null,
  val forecastNextExchangeDate: Long? = null,
  val comments: String? = null,
) : Parcelable

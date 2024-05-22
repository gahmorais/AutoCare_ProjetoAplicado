package br.com.gabrielmorais.autocare.data.models

import android.os.Parcelable
import android.os.SystemClock
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "manutencoes")
data class Maintenance(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: Int = SystemClock.uptimeMillis().toInt(),
  @ColumnInfo(name = "descricao")
  val description: String? = null,
  @ColumnInfo("data")
  val date: Long? = null,
  @ColumnInfo(name = "quilometragem_atual")
  val currentMileage: Int? = null,
  @ColumnInfo(name = "quilometragem_proxima_manutencao")
  val forecastNextExchangeMileage: Int? = null,
  @ColumnInfo(name = "data_proxima_manutencao")
  val forecastNextExchangeDate: Long? = null,
  @ColumnInfo(name = "observacoes")
  val comments: String? = null,
) : Parcelable

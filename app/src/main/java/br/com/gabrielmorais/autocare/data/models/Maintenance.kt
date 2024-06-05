package br.com.gabrielmorais.autocare.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
@Entity(
  tableName = "manutencoes",
  indices = [Index("veiculo_id")],
  foreignKeys = [ForeignKey(
    entity = Vehicle::class,
    parentColumns = ["veiculo_id"],
    childColumns = ["veiculo_id"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE
  )]
)
data class Maintenance(
  @PrimaryKey
  @ColumnInfo(name = "manutencao_id")
  val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "veiculo_id")
  val vehicleId: String,
  @ColumnInfo(name = "descricao")
  val description: String,
  @ColumnInfo("data")
  val date: Long,
  @ColumnInfo(name = "quilometragem_atual")
  val currentMileage: Int,
  @ColumnInfo(name = "quilometragem_proxima_manutencao")
  val forecastNextExchangeMileage: Int,
  @ColumnInfo(name = "data_proxima_manutencao")
  val forecastNextExchangeDate: Long,
  @ColumnInfo(name = "observacoes")
  val comments: String? = null,
) : Parcelable

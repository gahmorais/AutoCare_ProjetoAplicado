package br.com.gabrielmorais.autocare.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "veiculos",
  foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = ["usuario_id"],
    childColumns = ["usuario_id"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE
  )]
)
data class Vehicle(
  @PrimaryKey
  @ColumnInfo(name = "veiculo_id")
  val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "usuario_id")
  val userId: String,
  @ColumnInfo(name = "apelido")
  val nickName: String? = null,
  @ColumnInfo(name = "marca")
  val brand: String,
  @ColumnInfo(name = "modelo")
  val model: String,
  @ColumnInfo(name = "placa")
  val plate: String,
  @ColumnInfo(name = "foto")
  val photo: String? = null,
  @ColumnInfo("media_distancia_percorrida_mes")
  val averageDistanceTraveledPerMonth: Int
)


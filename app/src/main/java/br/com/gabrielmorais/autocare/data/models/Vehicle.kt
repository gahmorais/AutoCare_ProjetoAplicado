package br.com.gabrielmorais.autocare.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "veiculos")
data class Vehicle(
  @PrimaryKey
  @ColumnInfo(name = "id")
  val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "apelido")
  val nickName: String? = null,
  @ColumnInfo(name = "marca")
  val brand: String? = null,
  @ColumnInfo(name = "modelo")
  val model: String? = null,
  @ColumnInfo(name = "placa")
  val plate: String? = null,
  @ColumnInfo(name = "foto")
  val photo: String? = null,
  @ColumnInfo("media_distancia_percorrida_mes")
  val averageDistanceTraveledPerMonth: Int? = null
)


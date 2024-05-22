package br.com.gabrielmorais.autocare.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

@Entity(
  tableName = "usuarios", primaryKeys = [
    "id", "nickname"
  ]
)
data class User(
  @ColumnInfo(name = "id")
  val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "foto")
  val photo: String? = null,
  @ColumnInfo(name = "nickname")
  val nickname: String,
  @ColumnInfo(name = "nome")
  val name: String,
  @ColumnInfo(name = "senha")
  val password: String,
)

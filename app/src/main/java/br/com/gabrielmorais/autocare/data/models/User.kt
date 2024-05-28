package br.com.gabrielmorais.autocare.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "usuarios",
  indices = [Index("apelido", unique = true)],
)
data class User(
  @PrimaryKey
  @ColumnInfo(name = "usuario_id")
  val id: String = UUID.randomUUID().toString(),
  @ColumnInfo(name = "foto")
  val photo: String? = null,
  @ColumnInfo(name = "apelido")
  val nickname: String,
  @ColumnInfo(name = "nome")
  val name: String,
  @ColumnInfo(name = "senha")
  val password: String,
)

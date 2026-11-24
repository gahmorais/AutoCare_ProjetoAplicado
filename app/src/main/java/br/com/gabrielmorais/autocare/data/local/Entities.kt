package br.com.gabrielmorais.autocare.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val id: String,
  val name: String,
  @ColumnInfo(name = "photo_path") val photoPath: String?,
  @ColumnInfo(name = "created_at") val createdAt: Long
)

/**
 * Ids de perfil e de veiculo sao UUID, nao autoincremento: a importacao mescla
 * pelo id (etapa 8 do plano), e inteiros sequenciais de dois aparelhos colidiriam.
 */
@Entity(
  tableName = "vehicles",
  foreignKeys = [
    ForeignKey(
      entity = ProfileEntity::class,
      parentColumns = ["id"],
      childColumns = ["profile_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("profile_id")]
)
data class VehicleEntity(
  @PrimaryKey val id: String,
  @ColumnInfo(name = "profile_id") val profileId: String,
  @ColumnInfo(name = "nick_name") val nickName: String?,
  val brand: String?,
  val model: String?,
  val plate: String?,
  @ColumnInfo(name = "photo_path") val photoPath: String?,
  @ColumnInfo(name = "average_distance_per_month") val averageDistancePerMonth: Int?
)

/**
 * O id continua sendo o Int aleatorio de [br.com.gabrielmorais.autocare.data.models.Maintenance]
 * e nao um autoincremento: ele tambem e o requestCode do PendingIntent e o id da
 * notificacao, e precisa existir antes de a linha ser gravada.
 */
@Entity(
  tableName = "maintenances",
  foreignKeys = [
    ForeignKey(
      entity = VehicleEntity::class,
      parentColumns = ["id"],
      childColumns = ["vehicle_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("vehicle_id")]
)
data class MaintenanceEntity(
  @PrimaryKey val id: Int,
  @ColumnInfo(name = "vehicle_id") val vehicleId: String,
  val description: String?,
  /** Epoch day, como no modelo de dominio. */
  val date: Long?,
  @ColumnInfo(name = "current_mileage") val currentMileage: Int?,
  @ColumnInfo(name = "forecast_next_exchange_mileage") val forecastNextExchangeMileage: Int?,
  /** Epoch day, como no modelo de dominio. */
  @ColumnInfo(name = "forecast_next_exchange_date") val forecastNextExchangeDate: Long?,
  val comments: String?,
  val completed: Boolean
)

data class VehicleWithMaintenances(
  @Embedded val vehicle: VehicleEntity,
  @Relation(parentColumn = "id", entityColumn = "vehicle_id")
  val maintenances: List<MaintenanceEntity>
)

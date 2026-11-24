package br.com.gabrielmorais.autocare.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.local.VehicleEntity
import br.com.gabrielmorais.autocare.data.local.VehicleWithMaintenances
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
  // @Transaction porque @Relation roda uma segunda consulta para as manutencoes;
  // sem ela, uma escrita entre as duas devolveria um veiculo inconsistente.
  @Transaction
  @Query("SELECT * FROM vehicles WHERE profile_id = :profileId ORDER BY nick_name COLLATE NOCASE")
  fun observeByProfile(profileId: String): Flow<List<VehicleWithMaintenances>>

  @Transaction
  @Query("SELECT * FROM vehicles ORDER BY nick_name COLLATE NOCASE")
  fun observeAll(): Flow<List<VehicleWithMaintenances>>

  @Transaction
  @Query("SELECT * FROM vehicles WHERE id = :id")
  fun observe(id: String): Flow<VehicleWithMaintenances?>

  @Transaction
  @Query("SELECT * FROM vehicles WHERE id = :id")
  suspend fun get(id: String): VehicleWithMaintenances?

  @Transaction
  @Query("SELECT * FROM vehicles")
  suspend fun getAll(): List<VehicleWithMaintenances>

  /** Ver [ProfileDao.upsert]: REPLACE apagaria as manutencoes em cascata. */
  @Upsert
  suspend fun upsert(vehicle: VehicleEntity)

  @Upsert
  suspend fun upsertAll(vehicles: List<VehicleEntity>)

  @Query("DELETE FROM vehicles WHERE id = :id")
  suspend fun delete(id: String)

  /** @return linhas alteradas; 0 quando o veiculo nao existe. */
  @Query("UPDATE vehicles SET profile_id = :profileId WHERE id = :vehicleId")
  suspend fun updateOwner(vehicleId: String, profileId: String): Int
}

package br.com.gabrielmorais.autocare.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles ORDER BY created_at")
  fun observeAll(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM profiles WHERE id = :id")
  fun observe(id: String): Flow<ProfileEntity?>

  @Query("SELECT * FROM profiles WHERE id = :id")
  suspend fun get(id: String): ProfileEntity?

  @Query("SELECT * FROM profiles ORDER BY created_at")
  suspend fun getAll(): List<ProfileEntity>

  @Query("SELECT COUNT(*) FROM profiles")
  suspend fun count(): Int

  /**
   * Upsert e nao @Insert(REPLACE): o REPLACE do SQLite apaga a linha antes de
   * reinserir, e o ON DELETE CASCADE levaria junto todos os veiculos do perfil.
   */
  @Upsert
  suspend fun upsert(profile: ProfileEntity)

  @Upsert
  suspend fun upsertAll(profiles: List<ProfileEntity>)

  @Query("DELETE FROM profiles WHERE id = :id")
  suspend fun delete(id: String)
}

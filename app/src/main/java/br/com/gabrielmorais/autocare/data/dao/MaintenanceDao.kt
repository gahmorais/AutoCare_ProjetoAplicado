package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.models.Maintenance

@Dao
interface MaintenanceDao {
  @Upsert
  suspend fun create(maintenance: Maintenance)

  @Query("SELECT * FROM manutencoes WHERE manutencao_id = :id")
  suspend fun getById(id: String) : Maintenance

  @Update
  suspend fun update(maintenance: Maintenance):Int

  @Delete
  suspend fun delete(maintenance: Maintenance)
}
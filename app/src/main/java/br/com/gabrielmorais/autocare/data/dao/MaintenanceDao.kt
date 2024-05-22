package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.com.gabrielmorais.autocare.data.models.Maintenance

@Dao
interface MaintenanceDao {
  @Insert
  suspend fun create(maintenance: Maintenance)

  @Query("SELECT * FROM manutencoes WHERE id = :id")
  suspend fun getById(id: String) : Maintenance

  @Update
  suspend fun update(maintenance: Maintenance)

  @Delete
  suspend fun delete(maintenance: Maintenance)
}
package br.com.gabrielmorais.autocare.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.local.MaintenanceEntity

@Dao
interface MaintenanceDao {
  @Query("SELECT * FROM maintenances WHERE id = :id")
  suspend fun get(id: Int): MaintenanceEntity?

  @Upsert
  suspend fun upsert(maintenance: MaintenanceEntity)

  @Upsert
  suspend fun upsertAll(maintenances: List<MaintenanceEntity>)

  @Query("DELETE FROM maintenances WHERE id = :id")
  suspend fun delete(id: Int)

  /**
   * Lembretes a reagendar depois do reboot: de todos os perfis, nao so do ativo,
   * porque quem cuida dos carros da familia quer o aviso de todos eles.
   */
  @Query(
    "SELECT * FROM maintenances " +
      "WHERE completed = 0 AND forecast_next_exchange_date IS NOT NULL"
  )
  suspend fun getPendingWithForecast(): List<MaintenanceEntity>
}

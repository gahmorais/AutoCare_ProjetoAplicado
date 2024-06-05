package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

  @Upsert
  suspend fun create(vehicle: Vehicle)

  @Query("SELECT * FROM veiculos WHERE veiculo_id = :vehicleId")
  suspend fun getById(vehicleId: String): Vehicle

  @Update
  suspend fun update(vehicle: Vehicle): Int

  @Delete
  suspend fun delete(vehicle: Vehicle)

  @Transaction
  @Query("SELECT * FROM manutencoes WHERE veiculo_id = :vehicleId")
  fun getMaintenances(vehicleId: String): Flow<List<Maintenance>>

}
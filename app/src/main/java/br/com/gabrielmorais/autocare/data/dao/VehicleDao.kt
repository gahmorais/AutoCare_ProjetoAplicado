package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.models.VehicleMaintenances

@Dao
interface VehicleDao {

  @Insert
  suspend fun create(vehicle: Vehicle)

  @Query("SELECT * FROM veiculos WHERE id = :id")
  suspend fun getById(id: String) : Vehicle

  @Update
  suspend fun update(vehicle: Vehicle): Int

  @Delete
  suspend fun delete(vehicle: Vehicle)

  @Transaction
  @Query("SELECT * FROM  veiculos WHERE id = :id")
  suspend fun getVehicleMaintenances(id: String): VehicleMaintenances

}
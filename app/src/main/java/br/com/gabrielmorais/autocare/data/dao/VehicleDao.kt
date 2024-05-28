package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.com.gabrielmorais.autocare.data.models.Vehicle

@Dao
interface VehicleDao {

  @Insert
  suspend fun create(vehicle: Vehicle)

  @Query("SELECT * FROM veiculos WHERE usuario_id = :id")
  suspend fun getById(id: String) : Vehicle

  @Update
  suspend fun update(vehicle: Vehicle): Int

  @Delete
  suspend fun delete(vehicle: Vehicle)

//  @Transaction
//  @Query("SELECT * FROM  veiculos WHERE user_id = :id")
//  suspend fun getVehicleMaintenances(id: String)

}
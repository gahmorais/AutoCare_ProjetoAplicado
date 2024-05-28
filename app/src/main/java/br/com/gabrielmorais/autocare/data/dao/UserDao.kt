package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  /**
   *
   * Insere um usuário no sistema
   * @param user
   *
   * */
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun create(user: User)

  /**
   * Busca um usuário no sistema utilizando seu id
   * @param id
   * @return user?
   */
  @Query("SELECT * FROM usuarios WHERE usuario_id = :id")
  fun getById(id: String): User

  @Query("SELECT * FROM usuarios WHERE apelido = :nickname")
  suspend fun getUserByNickname(nickname: String): User?

  @Update
  suspend fun update(user: User): Int

  @Delete
  suspend fun delete(user: User)

  @Upsert
  suspend fun addVehicle(vehicle: Vehicle)

  @Query("SELECT * FROM veiculos WHERE veiculo_id = :id")
  suspend fun getVehicleById(id: String): Vehicle

  @Delete
  suspend fun deleteVehicle(vehicle: Vehicle)

  @Transaction
  @Query("SELECT * FROM veiculos WHERE usuario_id = :id")
  fun getVehicles(id: String): Flow<List<Vehicle>>
}
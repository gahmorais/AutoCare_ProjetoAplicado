package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  /**
   * Insere um usuário no sistema
   * @param user
   * */
  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun create(user: User)

  /**
   * Busca um usuário no sistema utilizando seu id
   * @param id
   * @return user
   */
  @Query("SELECT * FROM usuarios WHERE usuario_id = :id")
  fun getById(id: String): User


  /**
   * Busca um usuário no sistema utilizando seu nickname
   * @param nickname
   * @return user?
   */
  @Query("SELECT * FROM usuarios WHERE apelido = :nickname")
  suspend fun getUserByNickname(nickname: String): User?

  /**
   * Atualiza o usuário no sistema
   * @param user
   * @return Int
   */
  @Update
  suspend fun update(user: User): Int

  /**
   * Deleta o usuário no sistema
   * @param user
   */
  @Delete
  suspend fun delete(user: User)

  /**
   * Retorna todos os veículos cadastrados no id do usuário
   * @param userId
   * @return Int
   */
  @Transaction
  @Query("SELECT * FROM veiculos WHERE usuario_id = :userId")
  fun getVehicles(userId: String): Flow<List<Vehicle>>
}
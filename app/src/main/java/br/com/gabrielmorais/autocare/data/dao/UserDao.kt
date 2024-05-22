package br.com.gabrielmorais.autocare.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.UserVehicles

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
  @Query("SELECT * FROM usuarios WHERE id = :id")
  suspend fun getById(id: String): User?

  @Query("SELECT * FROM usuarios WHERE nickname = :nickname")
  suspend fun getUserByNickname(nickname: String): User?

  @Update
  suspend fun update(user: User): Int

  @Delete
  suspend fun delete(user: User)

  @Transaction
  @Query("SELECT * FROM usuarios WHERE id = :id")
  suspend fun getVehicles(id: String): UserVehicles
}
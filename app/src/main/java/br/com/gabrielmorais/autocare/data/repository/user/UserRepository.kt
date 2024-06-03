package br.com.gabrielmorais.autocare.data.repository.user

import android.database.sqlite.SQLiteConstraintException
import br.com.gabrielmorais.autocare.data.dao.UserDao
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber


class UserRepository(private val userDao: UserDao) {

  suspend fun createUser(user: User) = try {
    userDao.create(user)
  } catch (e: Exception) {
    throw e
  }

  fun create(user: User): Flow<Resource<User?>> {
    return flow {
      emit(Resource.loading(null))
      Timber.tag("UserRepository").i("Criando Usuário: $user")
      createUser(user)
      Timber.tag("UserRepository").i("Usuário Criado: $user")
      emit(Resource.success(user))
    }.catch { error ->
      val message = when (error) {
        is SQLiteConstraintException -> "O nome de usuário não pode ser utilizado"
        else -> error.message
      }

      emit(Resource.error(null, message ?: "Ocorreu um erro ao criar usuário"))
    }
  }

  fun getById(userId: String): User = try {
    userDao.getById(userId)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getByNickName(nickname: String) = try {
    userDao.getUserByNickname(nickname)
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(user: User) = try {
    userDao.update(user)
  } catch (e: Exception) {
    throw e
  }

  suspend fun delete(user: User) = try {
    userDao.delete(user)
  } catch (e: Exception) {
    throw e
  }

  fun getVehicles(userId: String): Flow<List<Vehicle>> = try {
    userDao.getVehicles(userId)
  } catch (e: Exception) {
    throw e
  }
}
package br.com.gabrielmorais.autocare.data.repository.user

import android.database.sqlite.SQLiteConstraintException
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber


class UserRepository(private val database: AppDatabase) {

  suspend fun create(user: User): Flow<Resource<User?>> {
    return flow {
      emit(Resource.loading(null))
      Timber.tag("UserRepository").i("Criando Usuário: $user")
      database.userDao().create(user)
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

  fun getById(id: String): User = try {
    database.userDao().getById(id)
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(user: User) = try {
    database.userDao().update(user)
  } catch (e: Exception) {
    throw e
  }

  fun getVehicles(id: String): Flow<List<Vehicle>> = try {
    database.userDao().getVehicles(id)
  } catch (e: Exception) {
    throw e
  }

  suspend fun getVehicleById(id: String) = try {
    database.userDao().getVehicleById(id)
  } catch (e: Exception) {
    throw e
  }

  suspend fun addVehicle(vehicle: Vehicle) = try {
    database.userDao().addVehicle(vehicle)
  } catch (e: Exception) {
    throw e
  }

  suspend fun deleteVehicle(vehicle: Vehicle) = try {
    database.userDao().deleteVehicle(vehicle)
  } catch (e: Exception) {
    throw e
  }

  fun changePassword(email: String, callback: (String) -> Unit) {
    TODO("Not yet implemented")
  }
}
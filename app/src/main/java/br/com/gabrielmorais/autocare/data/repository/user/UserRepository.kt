package br.com.gabrielmorais.autocare.data.repository.user

import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.User
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
      emit(Resource.error(null, error.message ?: "Ocorreu um erro ao criar usuário"))
    }
  }

  suspend fun getById(id: String): User? = try {
    database.userDao().getById(id) ?: throw Exception("Nenhum usuário encontrado")
  } catch (e: Exception) {
    throw e
  }

  suspend fun update(user: User) = try {
    database.userDao().update(user)
  } catch (e: Exception) {
    throw e
  }

  fun changePassword(email: String, callback: (String) -> Unit) {
    TODO("Not yet implemented")
  }
}
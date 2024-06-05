package br.com.gabrielmorais.autocare.data.repositories.user

import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repositories.Resource
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
  suspend fun createUser(user: User)
  fun create(user: User): Flow<Resource<User?>>
  fun getById(userId: String): User
  suspend fun getByNickName(nickname: String): User
  suspend fun update(user: User) : Int
  suspend fun delete(user: User)
  fun getVehicles(userId: String): Flow<List<Vehicle>>
}
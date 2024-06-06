package br.com.gabrielmorais.autocare.data.repositories.authorization


import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repositories.Resource
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
  fun login(nickname: String, password: String): Flow<Resource<User?>>
  fun getCurrentUser(): User?
  fun logout()
}
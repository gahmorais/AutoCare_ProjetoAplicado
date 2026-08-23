package br.com.gabrielmorais.autocare.fakes

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Fake sincrono: cada operacao devolve sucesso ou o erro configurado em
 * [failureToReturn], para exercitar os caminhos de erro sem tocar no Firebase.
 */
class FakeUserRepository : UserRepository {

  var failureToReturn: Throwable? = null
  val userFlow = MutableStateFlow<User?>(null)

  var savedVehicles = mutableListOf<Vehicle>()
  var deletedVehicleIds = mutableListOf<String>()
  var updatedUsers = mutableListOf<User>()
  var createdUsers = mutableListOf<User>()

  override fun createUser(user: User, callback: () -> Unit, onError: (Throwable) -> Unit) {
    failureToReturn?.let { return onError(it) }
    createdUsers.add(user)
    callback()
  }

  override fun updateUser(user: User, callback: (String) -> Unit, onError: (Throwable) -> Unit) {
    failureToReturn?.let { return onError(it) }
    updatedUsers.add(user)
    callback("Usuário atualizado com sucesso")
  }

  override fun observeUser(userId: String): Flow<User?> {
    failureToReturn?.let { error -> return flow { throw error } }
    return userFlow
  }

  override fun observeVehicles(userId: String): Flow<List<Vehicle>> = flow { emit(savedVehicles) }

  override fun saveVehicle(
    userId: String,
    vehicle: Vehicle,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    failureToReturn?.let { return onError(it) }
    savedVehicles.add(vehicle)
    callback("Veiculo salvo com sucesso")
  }

  override fun deleteVehicle(
    userId: String,
    vehicleId: String,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    failureToReturn?.let { return onError(it) }
    deletedVehicleIds.add(vehicleId)
    callback("Veículo deletado com sucesso")
  }

  override suspend fun saveUserPhoto(userId: String, image: Uri, callback: (String) -> Unit) {
    failureToReturn?.let { throw it }
    callback("https://exemplo.com/foto.jpg")
  }
}

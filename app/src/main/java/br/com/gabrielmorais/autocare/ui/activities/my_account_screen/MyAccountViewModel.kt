package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class MyAccountViewModel(
  private val userRepository: UserRepository,
  private val authRepository: AuthRepository
) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: Flow<User?> = _user

  private val _message = MutableStateFlow("")
  val message: Flow<String> = _message

  fun saveVehicle(userId: String, vehicle: Vehicle) {
    userRepository.saveVehicle(
      userId = userId,
      vehicle = vehicle,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun getUser(userId: String) {
    userRepository.getUser(
      userId = userId,
      callback = { _user.value = it },
      onError = ::publishError
    )
  }

  fun changePassword(email: String) {
    authRepository.changePassword(
      email = email,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun updateUser(user: User) {
    userRepository.updateUser(
      user = user,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun deleteVehicle(userId: String, vehicleId: String) {
    userRepository.deleteVehicle(
      userId = userId,
      vehicleId = vehicleId,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

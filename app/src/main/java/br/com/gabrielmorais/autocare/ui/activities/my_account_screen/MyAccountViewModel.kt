package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repositories.user.IUserRepository
import br.com.gabrielmorais.autocare.data.repositories.vehicleRepository.IVehicleRepository
import br.com.gabrielmorais.autocare.utils.handleException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MyAccountViewModel(
  private val userRepository: IUserRepository,
  private val vehicleRepository: IVehicleRepository
) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user = _user.asStateFlow()

  private val _message = MutableStateFlow("")
  val message = _message.asStateFlow()

  private val _vehicleList = MutableStateFlow<List<Vehicle>>(listOf())
  val vehicleList = _vehicleList.asStateFlow()

  suspend fun saveVehicle(vehicle: Vehicle) = try {
    Timber.tag("MyAccountViewModel").i("Adicionando veículo $vehicle")
    vehicleRepository.create(vehicle)
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  private fun emitMessage(text: String) = viewModelScope.launch {
    _message.emit(text)
  }

  fun getUser(userId: String) = try {
    val currentUser = userRepository.getById(userId)
    userRepository.getVehicles(currentUser.id).onEach { vehicles ->
      _vehicleList.update { vehicles }
    }.launchIn(viewModelScope)
    _user.update { currentUser }
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  fun changePassword(email: String) {

  }

  suspend fun updateUser(user: User) = try {
    userRepository.update(user)
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  suspend fun deleteVehicle(vehicle: Vehicle) = try {
    vehicleRepository.delete(vehicle)
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }


}
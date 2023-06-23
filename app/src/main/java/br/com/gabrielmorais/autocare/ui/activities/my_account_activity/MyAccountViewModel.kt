package br.com.gabrielmorais.autocare.ui.activities.my_account_activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyAccountViewModel(
  private val userRepository: UserRepository
) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: Flow<User?> = _user

  private val _message = MutableStateFlow("")
  val message: Flow<String> = _message

  fun saveVehicle(userId: String, vehicle: Vehicle) {
    userRepository.saveVehicle(userId, vehicle) {
      viewModelScope.launch { _message.emit(it) }
    }
  }

  fun getUser(userId: String) {
    try {
      userRepository.getUser(userId) {
        viewModelScope.launch { _user.emit(it) }
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

}
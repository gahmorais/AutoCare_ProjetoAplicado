package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyAccountViewModel(
  private val userRepository: UserRepositoryImpl
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

  fun changePassword(email: String) {
    try {
      val authRepository = AuthRepositoryImpl(Firebase.auth)
      authRepository.changePassword(email) {
        _message.value = it
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun updateUser(user: User) {
    try {
      userRepository.updateUser(user) {
        _message.value = it
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun deleteVehicle(userId: String, vehicleId: String) {
    try {
      userRepository.deleteVehicle(userId, vehicleId) {
        _message.value = it
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

}
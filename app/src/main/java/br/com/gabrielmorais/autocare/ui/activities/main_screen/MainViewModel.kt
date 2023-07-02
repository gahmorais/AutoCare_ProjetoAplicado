package br.com.gabrielmorais.autocare.ui.activities.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
  private val authRepository: AuthRepositoryImpl,
  private val userRepository: UserRepositoryImpl
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: Flow<User?> = _user
  fun logout() = authRepository.logout()

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
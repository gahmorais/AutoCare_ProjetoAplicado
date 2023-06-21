package br.com.gabrielmorais.autocare.ui.activities.main_screen

import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.autocare.data.repository.UserRepository
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository

class MainViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository
) : ViewModel() {
  fun logout() = authRepository.logout()
  fun getUser(userId: String) {

  }
}
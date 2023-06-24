package br.com.gabrielmorais.autocare.ui.activities.main_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository

class MainViewModelFactory(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return MainViewModel(authRepository, userRepository) as T
  }
}
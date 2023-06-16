package br.com.gabrielmorais.autocare.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gabrielmorais.autocare.data.repository.UserRepository
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.ui.viewmodels.RegisterViewModel

class RegisterViewModelFactory(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository
) :
  ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return RegisterViewModel(authRepository, userRepository) as T
  }
}
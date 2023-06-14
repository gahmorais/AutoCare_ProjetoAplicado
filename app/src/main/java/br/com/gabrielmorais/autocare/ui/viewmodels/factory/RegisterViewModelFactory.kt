package br.com.gabrielmorais.autocare.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gabrielmorais.autocare.ui.authorization.AuthRepository
import br.com.gabrielmorais.autocare.ui.viewmodels.RegisterViewModel

class RegisterViewModelFactory(private val authRepository: AuthRepository) :
  ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return RegisterViewModel(authRepository) as T
  }
}
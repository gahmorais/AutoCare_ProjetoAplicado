package br.com.gabrielmorais.autocare.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.gabrielmorais.autocare.ui.authorization.AuthRepository
import br.com.gabrielmorais.autocare.ui.viewmodels.LoginViewModel

class LoginViewModelFactory(private val authRepository: AuthRepository) :
  ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return LoginViewModel(authRepository) as T
  }
}
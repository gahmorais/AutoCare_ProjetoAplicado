package br.com.gabrielmorais.autocare.ui.viewmodels

import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository

class MainViewModel(private val authRepository: AuthRepository) : ViewModel() {
  fun logout() = authRepository.logout()
}
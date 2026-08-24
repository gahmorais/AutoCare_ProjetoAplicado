package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user: Flow<User?> = _user

  private val _message = MutableStateFlow("")
  val message: Flow<String> = _message

  private var observeUserJob: Job? = null

  /** Sempre a sessao autenticada - nunca um id vindo da UI ou de uma Intent. */
  private val currentUserId: String?
    get() = authRepository.currentUserId()

  fun logout() = authRepository.logout()

  /**
   * Idempotente: era chamado a cada onResume e cada chamada deixava um
   * ValueEventListener ativo para sempre. O Job anterior e cancelado e o
   * awaitClose do callbackFlow remove o listener.
   */
  fun observeUser() {
    // O uid vem da sessao autenticada, nao de um extra de Intent que qualquer
    // app podia forjar.
    val userId = authRepository.currentUserId()
    if (userId == null) {
      publishError(IllegalStateException("Sessão expirada"))
      return
    }
    observeUserJob?.cancel()
    observeUserJob = viewModelScope.launch {
      userRepository.observeUser(userId)
        .catch { publishError(it) }
        .collect { _user.value = it }
    }
  }

  fun updateUserPhoto(image: Uri) {
    viewModelScope.launch {
      // saveUserPhoto suspende e propaga a excecao do upload; sem o catch a
      // excecao escapa do viewModelScope e derruba o app.
      runCatching {
        val imageUrl = userRepository.saveUserPhoto(image)
        _user.value?.let { current ->
          userRepository.updateUser(
            user = current.copy(photo = imageUrl),
            callback = { _message.value = it },
            onError = ::publishError
          )
        }
      }.onFailure(::publishError)
    }
  }

  fun saveVehicle(vehicle: Vehicle) {
    val userId = currentUserId ?: return publishError(IllegalStateException("Sessão expirada"))
    userRepository.saveVehicle(
      userId = userId,
      vehicle = vehicle,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun deleteVehicle(vehicleId: String) {
    val userId = currentUserId ?: return publishError(IllegalStateException("Sessão expirada"))
    userRepository.deleteVehicle(
      userId = userId,
      vehicleId = vehicleId,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun changePassword(email: String) {
    authRepository.changePassword(
      email = email,
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  fun updateUser(user: User) {
    val userId = currentUserId ?: return publishError(IllegalStateException("Sessão expirada"))
    userRepository.updateUser(
      user = user.copy(id = userId),
      callback = { _message.value = it },
      onError = ::publishError
    )
  }

  private fun publishError(error: Throwable) {
    _message.value = error.message ?: "Ocorreu um erro inesperado"
  }
}

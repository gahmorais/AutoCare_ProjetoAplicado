package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repositories.authorization.IAuthRepository
import br.com.gabrielmorais.autocare.data.repositories.user.IUserRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.handleException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
  private val authRepository: IAuthRepository,
  private val userRepository: IUserRepository,
  private val imageUtils: ImageUtils,
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user = _user.asStateFlow()

  private val _vehicleList = MutableStateFlow<List<Vehicle>>(listOf())
  val vehicleList = _vehicleList.asStateFlow()

  private val _message = MutableSharedFlow<String>()
  val message = _message.asSharedFlow()

  fun logout() {
    authRepository.logout()
  }

  fun getUser(userId: String) = try {
    val currentUser = userRepository.getById(userId)
    Timber.tag("MainViewModel").i("Usuário: $currentUser")
    _user.update { currentUser }
    userRepository.getVehicles(currentUser.id).onEach { vehicles ->
      _vehicleList.update { vehicles }
    }.launchIn(viewModelScope)
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  suspend fun updateUserPhoto(userId: String, image: Uri) = try {
    val imagePath = imageUtils.saveImage(
      userId,
      image
    )
    val userUpdated = _user.value?.copy(photo = imagePath) ?: throw Exception("Usuário nulo")
    userRepository.update(userUpdated)
    _user.update { userUpdated }
  } catch (e: Exception) {
    e.handleException { emitMessage(it) }
  }

  private fun emitMessage(text: String) = viewModelScope.launch {
    _message.emit(text)
  }

}
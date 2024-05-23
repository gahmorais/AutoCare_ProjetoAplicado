package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.ResourceProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val imageUtils: ImageUtils,
  private val resourceProvider: ResourceProvider
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user = _user.asStateFlow()

  private val _message = MutableSharedFlow<String>()
  val message = _message.asSharedFlow()

  fun logout() {
    authRepository.logout()
  }

  suspend fun getUser(userId: String) = try {
    val currentUser = userRepository.getById(userId)
    Timber.tag("MainViewModel").i("Usuário: $currentUser")
    currentUser?.let { _user.emit(currentUser) }
  } catch (e: Exception) {
    handleExcetion(e)
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
    handleExcetion(e)
  }

  private fun handleExcetion(t: Throwable) {
    t.printStackTrace()
    emitMessage(t.message ?: "Ocorreu um erro")
  }

  private fun emitMessage(text: String) = viewModelScope.launch {
    _message.emit(text)
  }

}
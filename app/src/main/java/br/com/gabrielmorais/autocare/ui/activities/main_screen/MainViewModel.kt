package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class MainViewModel(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
  private val imageUtils: ImageUtils
) : ViewModel() {
  private val _user = MutableStateFlow<User?>(null)
  val user = _user.asStateFlow()
  fun logout() {
    authRepository.logout()
  }

  suspend fun getUser(userId: String) = try {
    val currentUser = userRepository.getById(userId)
    Timber.tag("MainViewModel").i("Usuário: $currentUser")
    currentUser?.let { _user.emit(currentUser) }
  } catch (e: Exception) {
    e.printStackTrace()
  }

  suspend fun updateUserPhoto(userId: String, image: Uri) = try {
    val imagePath = imageUtils.saveImage(userId, image)
    val userUpdated = _user.value?.copy(photo = imagePath) ?: throw Exception("Usuário nulo")
    userRepository.update(userUpdated)
    _user.emit(userUpdated)
  } catch (e: Exception) {
    e.printStackTrace()
  }

}
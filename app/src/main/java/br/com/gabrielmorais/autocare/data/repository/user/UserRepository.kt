package br.com.gabrielmorais.autocare.data.repository.user

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle

/**
 * Toda operacao recebe um [onError] porque os callbacks do Firebase sao assincronos:
 * lancar excecao de dentro deles nao e capturavel por try/catch no chamador e derruba
 * o processo. O erro precisa voltar pelo callback.
 */
interface UserRepository {
  fun createUser(user: User, callback: () -> Unit, onError: (Throwable) -> Unit)
  fun getUser(userId: String, callback: (User?) -> Unit, onError: (Throwable) -> Unit)
  fun updateUser(user: User, callback: (String) -> Unit, onError: (Throwable) -> Unit)
  fun getVehicles(userId: String, callback: (List<Vehicle>) -> Unit, onError: (Throwable) -> Unit)
  fun saveVehicle(
    userId: String,
    vehicle: Vehicle,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  fun deleteVehicle(
    userId: String,
    vehicleId: String,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  suspend fun saveUserPhoto(userId: String, image: Uri, callback: (String) -> Unit)
}

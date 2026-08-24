package br.com.gabrielmorais.autocare.data.repository.user

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

/**
 * Toda operacao recebe um [onError] porque os callbacks do Firebase sao assincronos:
 * lancar excecao de dentro deles nao e capturavel por try/catch no chamador e derruba
 * o processo. O erro precisa voltar pelo callback.
 */
interface UserRepository {
  fun createUser(user: User, callback: () -> Unit, onError: (Throwable) -> Unit)
  fun updateUser(user: User, callback: (String) -> Unit, onError: (Throwable) -> Unit)

  /**
   * Usuario com seus veiculos. Como Flow, o ValueEventListener e removido em
   * awaitClose quando o coletor sai de cena; a versao com callback registrava um
   * listener a cada chamada e nunca o removia.
   */
  fun observeUser(userId: String): Flow<User?>

  fun observeVehicles(userId: String): Flow<List<Vehicle>>

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

  /**
   * Hospeda a foto de perfil e devolve a URL publica. Nao recebe mais userId: o
   * host gera o identificador do asset e o caminho por usuario deixou de existir.
   */
  suspend fun saveUserPhoto(image: Uri): String
}

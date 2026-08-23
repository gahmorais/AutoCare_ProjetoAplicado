package br.com.gabrielmorais.autocare.data.repository.user

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants
import br.com.gabrielmorais.autocare.utils.Constants.Companion.USER_CHILD
import br.com.gabrielmorais.autocare.utils.Constants.Companion.VEHICLE_CHILD
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
  private val database: FirebaseDatabase,
  private val storage: FirebaseStorage
) : UserRepository {
  override fun createUser(user: User, callback: () -> Unit, onError: (Throwable) -> Unit) {
    val userId = user.id
    if (userId.isNullOrBlank()) {
      onError(IllegalArgumentException("Usuário sem identificador"))
      return
    }
    database.reference
      .child(USER_CHILD)
      .child(userId)
      .setValue(user)
      .addOnSuccessListener {
        callback()
      }
      .addOnFailureListener(onError)
  }

  /**
   * A versao anterior aninhava getVehicles dentro do onDataChange do usuario:
   * cada emissao do listener de usuario registrava mais um listener de veiculos,
   * nenhum deles removido. Combinar dois Flows independentes evita isso.
   */
  override fun observeUser(userId: String): Flow<User?> =
    combine(observeUserNode(userId), observeVehicles(userId)) { user, vehicles ->
      user?.copy(vehicles = vehicles)
    }

  private fun observeUserNode(userId: String): Flow<User?> = callbackFlow {
    val reference = database.reference.child(USER_CHILD).child(userId)
    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        trySend(runCatching { snapshot.getValue<User>() }.getOrNull())
      }

      override fun onCancelled(error: DatabaseError) {
        close(error.toException())
      }
    }
    reference.addValueEventListener(listener)
    awaitClose { reference.removeEventListener(listener) }
  }

  override fun updateUser(user: User, callback: (String) -> Unit, onError: (Throwable) -> Unit) {
    val userId = user.id
    if (userId.isNullOrBlank()) {
      onError(IllegalArgumentException("Usuário sem identificador"))
      return
    }
    database
      .reference
      .child(USER_CHILD)
      .child(userId)
      .setValue(user)
      .addOnSuccessListener {
        callback("Usuário atualizado com sucesso")
      }
      .addOnFailureListener(onError)
  }

  override fun observeVehicles(userId: String): Flow<List<Vehicle>> = callbackFlow {
    val reference = database.reference.child(VEHICLE_CHILD).child(userId)
    val listener = object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        // A montagem campo a campo anterior ignorava 'maintenances': qualquer
        // codigo que regravasse esse objeto apagaria o historico.
        trySend(snapshot.children.mapNotNull { child ->
          runCatching { child.getValue<Vehicle>() }.getOrNull()
        })
      }

      override fun onCancelled(error: DatabaseError) {
        close(error.toException())
      }
    }
    reference.addValueEventListener(listener)
    awaitClose { reference.removeEventListener(listener) }
  }

  override fun saveVehicle(
    userId: String,
    vehicle: Vehicle,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    val vehicleId = vehicle.id
    if (vehicleId.isNullOrBlank()) {
      onError(IllegalArgumentException("Veículo sem identificador"))
      return
    }
    database
      .reference
      .child(VEHICLE_CHILD)
      .child(userId)
      .child(vehicleId)
      .setValue(vehicle)
      .addOnSuccessListener {
        callback("Veiculo salvo com sucesso")
      }
      .addOnFailureListener(onError)
  }

  override fun deleteVehicle(
    userId: String,
    vehicleId: String,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    database
      .reference
      .child(VEHICLE_CHILD)
      .child(userId)
      .child(vehicleId)
      .removeValue()
      .addOnSuccessListener {
        callback("Veículo deletado com sucesso")
      }
      .addOnFailureListener(onError)
  }

  override suspend fun saveUserPhoto(
    userId: String,
    image: Uri,
    callback: (String) -> Unit
  ) {
    val uploadTask = storage.reference
      .child(userId)
      .child(Constants.PROFILE_PHOTO_PATH)
      .child(userId)
      .putFile(image)
      .await()

    if (uploadTask.task.isSuccessful) {
      val imageUrl = uploadTask.storage.downloadUrl.await()
      callback(imageUrl.toString())
    } else {
      uploadTask.task.exception?.let { error ->
        throw error
      }
    }

  }
}

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
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
  private val database: FirebaseDatabase,
  private val storage: FirebaseStorage
) : UserRepository {
  override fun createUser(user: User, callback: () -> Unit) {
    database.reference
      .child(USER_CHILD)
      .child(user.id ?: "")
      .setValue(user)
      .addOnSuccessListener {
        callback()
      }
      .addOnFailureListener { error ->
        throw error
      }
  }

  override fun getUser(userId: String, callback: (User?) -> Unit) {
    database
      .reference
      .child(USER_CHILD)
      .child(userId)
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          try {
            val user = snapshot.getValue<User>()
            getVehicles(user?.id!!) { vehicles ->
              val updatedUser = user.copy(vehicles = vehicles)
              callback(updatedUser)
            }
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }

        override fun onCancelled(error: DatabaseError) {

        }
      })
  }

  override fun updateUser(user: User, callback: (String) -> Unit) {
    database
      .reference
      .child(USER_CHILD)
      .child(user.id ?: "")
      .setValue(user)
      .addOnSuccessListener {
        callback("Usuário atualizado com sucesso")
      }
      .addOnFailureListener { error ->
        throw error
      }
  }

  override fun getVehicles(userId: String, callback: (List<Vehicle>) -> Unit) {
    database
      .reference
      .child(VEHICLE_CHILD)
      .child(userId)
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val vehicleChildren = snapshot.children
          val iterator = vehicleChildren.iterator()
          val vehicleList = mutableListOf<Vehicle>()
          while (iterator.hasNext()) {
            val i = iterator.next()
            val brand = i.child("brand").getValue<String>()
            val model = i.child("model").getValue<String>()
            val plate = i.child("plate").getValue<String>()
            val photo = i.child("photo").getValue<String?>()
            val id = i.child("id").getValue<String>()
            val nickName = i.child("nickName").getValue<String>()
            val averageDistanceTraveledPerMonth = i.child("averageDistanceTraveledPerMonth").getValue<Int>()

            val vehicle = Vehicle(
              id = id,
              brand = brand,
              model = model,
              plate = plate,
              photo = photo,
              nickName = nickName,
              averageDistanceTraveledPerMonth = averageDistanceTraveledPerMonth
            )

            vehicleList.add(vehicle)
          }
          callback(vehicleList)
        }

        override fun onCancelled(error: DatabaseError) {
          throw error.toException()
        }
      })
  }

  override fun saveVehicle(userId: String, vehicle: Vehicle, callback: (String) -> Unit) {
    database
      .reference
      .child(VEHICLE_CHILD)
      .child(userId)
      .child(vehicle.id!!)
      .setValue(vehicle)
      .addOnSuccessListener {
        callback("Veiculo salvo com sucesso")
      }
      .addOnFailureListener { error ->
        throw error
      }
  }

  override fun deleteVehicle(userId: String, vehicleId: String, callback: (String) -> Unit) {
    database
      .reference
      .child(VEHICLE_CHILD)
      .child(userId)
      .child(vehicleId)
      .removeValue()
      .addOnSuccessListener {
        callback("Veículo deletado com sucesso")
      }
      .addOnFailureListener { error ->
        throw error
      }
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
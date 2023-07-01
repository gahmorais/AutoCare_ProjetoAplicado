package br.com.gabrielmorais.autocare.data.repository.vehicleRepository

import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class VehicleRepositoryImpl(
  private val storage: FirebaseStorage,
  private val database: FirebaseDatabase
) : VehicleRepository {
  override suspend fun saveVehicleImage(
    userId: String,
    vehicleId: String,
    image: ByteArray,
    callback: (String) -> Unit
  ) {
    val uploadTask = storage.reference
      .child(userId)
      .child(vehicleId)
      .putBytes(image)
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

  override fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSucess: (Vehicle) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    database.reference
      .child(Constants.VEHICLE_CHILD)
      .child(userId)
      .child(vehicleId)
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          if (snapshot.exists()) {
            val vehicle = snapshot.getValue<Vehicle>()
            vehicle?.let(onSucess)
          }
        }

        override fun onCancelled(error: DatabaseError) {
          onError(error.toException())
        }
      })
  }
}
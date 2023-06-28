package br.com.gabrielmorais.autocare.data.repository

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class VehicleRepository(private val storage: FirebaseStorage) {
  suspend fun saveVehicleImage(
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
}
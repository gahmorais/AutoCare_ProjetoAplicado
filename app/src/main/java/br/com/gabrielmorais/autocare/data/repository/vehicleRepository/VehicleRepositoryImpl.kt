package br.com.gabrielmorais.autocare.data.repository.vehicleRepository

import android.net.Uri
import br.com.gabrielmorais.autocare.data.images.ImageUploader
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

class VehicleRepositoryImpl(
  private val imageUploader: ImageUploader,
  private val database: FirebaseDatabase
) : VehicleRepository {
  override suspend fun saveVehicleImage(image: Uri): String = imageUploader.upload(image)

  override suspend fun getVehiclesOnce(userId: String): List<Vehicle> {
    val snapshot = database.reference
      .child(Constants.VEHICLE_CHILD)
      .child(userId)
      .get()
      .await()
    return snapshot.children.mapNotNull { it.getValue<Vehicle>() }
  }

  override fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSuccess: (Vehicle) -> Unit,
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
            vehicle?.let(onSuccess)
          }
        }

        override fun onCancelled(error: DatabaseError) {
          onError(error.toException())
        }
      })
  }

  override fun updateVehicle(
    userId: String,
    vehicleId: String,
    vehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    database.reference
      .child(Constants.VEHICLE_CHILD)
      .child(userId)
      .child(vehicleId)
      .setValue(vehicle)
      .addOnSuccessListener {
        onSuccess("Veiculo atualizado")
      }
      .addOnFailureListener {
        onError(it)
      }
  }
}
package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants.Companion.VEHICLE_CHILD
import com.google.firebase.database.FirebaseDatabase


class MaintenanceRepositoryImpl(private val database: FirebaseDatabase) : MaintenanceRepository {
  override fun create(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    database.reference
      .child("${VEHICLE_CHILD}/${userId}/${vehicleId}")
      .setValue(updatedVehicle)
      .addOnSuccessListener {
        onSuccess("Manutenção cadastrada")
      }
      .addOnFailureListener { error ->
        onError(error)
      }
  }

}
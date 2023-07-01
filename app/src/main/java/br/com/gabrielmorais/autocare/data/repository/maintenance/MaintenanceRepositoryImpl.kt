package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.utils.Constants.Companion.MAINTENANCE_CHILD
import com.google.firebase.database.FirebaseDatabase

private const val VEHICLE_CHILD = "vehicles"


class MaintenanceRepositoryImpl(private val database: FirebaseDatabase) : MaintenanceRepository {
  override fun create(
    userId: String,
    vehicleId: String,
    maintenance: Maintenance,
    callback: (String) -> Unit
  ) {
    database.reference
      .child("${VEHICLE_CHILD}/${userId}/${vehicleId}/${MAINTENANCE_CHILD}")
      .push()
      .setValue(maintenance)
      .addOnSuccessListener {
        callback("Manutenção cadastrada")
      }
      .addOnFailureListener { error ->
        throw error
      }
  }

}
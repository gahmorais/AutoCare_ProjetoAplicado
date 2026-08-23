package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants.Companion.VEHICLE_CHILD
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue


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

  /**
   * A versao anterior montava a referencia e descartava: excluir manutencao nao
   * fazia nada. As manutencoes sao uma lista dentro do veiculo, entao a remocao
   * e feita relendo o veiculo e regravando a lista sem o item.
   */
  override fun delete(
    userId: String,
    vehicleId: String,
    maintenanceId: Int,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    val reference = database.reference.child("$VEHICLE_CHILD/$userId/$vehicleId")

    reference.get()
      .addOnSuccessListener { snapshot ->
        val vehicle = runCatching { snapshot.getValue<Vehicle>() }.getOrNull()
        if (vehicle == null) {
          onError(IllegalStateException("Veículo não encontrado"))
          return@addOnSuccessListener
        }

        val remaining = vehicle.maintenances.orEmpty().filterNot { it.id == maintenanceId }
        if (remaining.size == vehicle.maintenances.orEmpty().size) {
          onError(IllegalStateException("Manutenção não encontrada"))
          return@addOnSuccessListener
        }

        reference.setValue(vehicle.copy(maintenances = remaining))
          .addOnSuccessListener { onSuccess("Manutenção removida") }
          .addOnFailureListener(onError)
      }
      .addOnFailureListener(onError)
  }
}

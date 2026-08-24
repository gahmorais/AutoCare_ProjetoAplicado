package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.utils.Constants.Companion.VEHICLE_CHILD
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

/**
 * Remove a manutencao de [maintenanceId], ou null quando ela nao esta na lista.
 * Funcao pura para poder ser testada sem Firebase.
 */
internal fun List<Maintenance>.removingById(maintenanceId: Int): List<Maintenance>? {
  val remaining = filterNot { it.id == maintenanceId }
  return if (remaining.size == size) null else remaining
}

/**
 * Troca a manutencao de mesmo id por [maintenance], preservando a posicao, ou
 * null quando o id nao existe na lista.
 */
internal fun List<Maintenance>.replacingById(maintenance: Maintenance): List<Maintenance>? {
  if (none { it.id == maintenance.id }) return null
  return map { if (it.id == maintenance.id) maintenance else it }
}

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

  override fun update(
    userId: String,
    vehicleId: String,
    maintenance: Maintenance,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = mutateMaintenances(
    userId = userId,
    vehicleId = vehicleId,
    successMessage = "Manutenção atualizada",
    onSuccess = onSuccess,
    onError = onError,
    transform = { it.replacingById(maintenance) }
  )

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
  ) = mutateMaintenances(
    userId = userId,
    vehicleId = vehicleId,
    successMessage = "Manutenção removida",
    onSuccess = onSuccess,
    onError = onError,
    transform = { it.removingById(maintenanceId) }
  )

  /**
   * Ciclo ler-modificar-gravar do veiculo. [transform] devolve null quando a
   * manutencao alvo nao esta na lista, e nesse caso nada e gravado.
   */
  private fun mutateMaintenances(
    userId: String,
    vehicleId: String,
    successMessage: String,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit,
    transform: (List<Maintenance>) -> List<Maintenance>?
  ) {
    val reference = database.reference.child("$VEHICLE_CHILD/$userId/$vehicleId")

    reference.get()
      .addOnSuccessListener { snapshot ->
        val vehicle = runCatching { snapshot.getValue<Vehicle>() }.getOrNull()
        if (vehicle == null) {
          onError(IllegalStateException("Veículo não encontrado"))
          return@addOnSuccessListener
        }

        val updated = transform(vehicle.maintenances.orEmpty())
        if (updated == null) {
          onError(IllegalStateException("Manutenção não encontrada"))
          return@addOnSuccessListener
        }

        reference.setValue(vehicle.copy(maintenances = updated))
          .addOnSuccessListener { onSuccess(successMessage) }
          .addOnFailureListener(onError)
      }
      .addOnFailureListener(onError)
  }
}

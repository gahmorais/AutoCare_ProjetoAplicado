package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle

interface MaintenanceRepository {
  fun create(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  /**
   * Substitui a manutencao de mesmo [Maintenance.id]. Diferente de [create], le
   * o veiculo antes de gravar em vez de receber a lista ja montada pela UI.
   */
  fun update(
    userId: String,
    vehicleId: String,
    maintenance: Maintenance,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  /**
   * Devolve uma manutencao excluida. Serve ao desfazer do snackbar: a exclusao
   * ja gravou, entao voltar atras e regravar.
   */
  fun restore(
    userId: String,
    vehicleId: String,
    maintenance: Maintenance,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )

  fun delete(
    userId: String,
    vehicleId: String,
    maintenanceId: Int,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )
}

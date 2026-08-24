package br.com.gabrielmorais.autocare.data.repository.vehicleRepository

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.Vehicle

interface VehicleRepository {
  /**
   * Hospeda a imagem e devolve a URL publica. Nao recebe mais userId/vehicleId:
   * o host gera o identificador do asset e o caminho deixou de existir.
   */
  suspend fun saveVehicleImage(image: Uri): String
  /**
   * Leitura pontual (sem listener) de todos os veiculos do usuario, incluindo as
   * manutencoes. Usada pelo reagendamento das notificacoes apos o reboot.
   */
  suspend fun getVehiclesOnce(userId: String): List<Vehicle>

  fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSuccess: (Vehicle) -> Unit,
    onError: (Throwable) -> Unit
  )
  fun updateVehicle(
    userId:String,
    vehicleId:String,
    vehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  )
}
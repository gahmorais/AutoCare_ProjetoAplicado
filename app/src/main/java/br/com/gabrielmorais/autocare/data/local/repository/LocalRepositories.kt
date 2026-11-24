package br.com.gabrielmorais.autocare.data.local.repository

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.Flow

/*
 * Repositorios do armazenamento local. Diferente dos do Firebase, nao recebem
 * userId (nao ha sessao) nem callbacks: escrita e suspend e leitura continua e
 * Flow, e o erro chega ao chamador como excecao comum, capturavel com runCatching.
 *
 * Convivem com os do Firebase ate a etapa 4 do plano, quando as ViewModels
 * passam a usar estes, e a etapa 5 apaga os antigos.
 */

interface ProfileRepository {
  /** Em ordem de criacao: o primeiro perfil criado aparece primeiro. */
  fun observeProfiles(): Flow<List<Profile>>
  fun observeProfile(id: String): Flow<Profile?>
  suspend fun getProfile(id: String): Profile?
  suspend fun countProfiles(): Int
  suspend fun saveProfile(profile: Profile)

  /** Apaga o perfil e, em cascata, os veiculos e as manutencoes dele. */
  suspend fun deleteProfile(id: String)
}

interface VehicleRepository {
  fun observeVehicles(profileId: String): Flow<List<Vehicle>>

  /** De todos os perfis: alimenta as pendencias do Inicio. */
  fun observeAllVehicles(): Flow<List<Vehicle>>
  fun observeVehicle(id: String): Flow<Vehicle?>
  suspend fun getVehicle(id: String): Vehicle?

  /**
   * Grava os dados do veiculo. As manutencoes de [vehicle] sao ignoradas e as
   * ja gravadas ficam intactas: manutencao se grava pelo [MaintenanceRepository].
   *
   * @throws IllegalArgumentException se o veiculo nao tiver id ou perfil dono.
   */
  suspend fun saveVehicle(vehicle: Vehicle)

  /** Apaga o veiculo e, em cascata, as manutencoes dele. */
  suspend fun deleteVehicle(id: String)

  /**
   * Regrava um veiculo excluido junto com as manutencoes, com os mesmos ids.
   * Serve ao desfazer do snackbar; e atomico, ou volta tudo ou nada.
   */
  suspend fun restoreVehicle(vehicle: Vehicle)

  /**
   * Passa o veiculo, com todo o historico, para outro perfil.
   *
   * @throws NoSuchElementException se o veiculo nao existir.
   * @throws android.database.sqlite.SQLiteConstraintException se o perfil de
   * destino nao existir.
   */
  suspend fun transferVehicle(vehicleId: String, toProfileId: String)
}

interface MaintenanceRepository {
  /** Cria ou atualiza pelo id. Tambem serve ao desfazer da exclusao. */
  suspend fun saveMaintenance(vehicleId: String, maintenance: Maintenance)
  suspend fun getMaintenance(id: Int): Maintenance?
  suspend fun deleteMaintenance(id: Int)

  /** Nao concluidas e com data prevista, de todos os perfis. */
  suspend fun getPendingWithForecast(): List<Maintenance>
}

interface ServicesRepository {
  suspend fun getServices(): List<Service>
}

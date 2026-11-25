package br.com.gabrielmorais.autocare.data.backup

import androidx.room.withTransaction
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.toDomain
import br.com.gabrielmorais.autocare.data.local.toEntity

/**
 * O que o backup precisa do banco: ler tudo de uma vez e mesclar tudo de uma
 * vez. Interface para o exportador e o importador serem testados na JVM, onde o
 * Room nao roda.
 */
interface BackupStore {
  /** Todos os perfis, veiculos e manutencoes, lidos numa unica transacao. */
  suspend fun snapshot(): BackupSnapshot

  /**
   * Cria ou atualiza pelo id tudo o que esta em [snapshot], numa unica
   * transacao: ou entra tudo, ou nada. Nada que ja esta no banco e fora do
   * snapshot e apagado.
   */
  suspend fun merge(snapshot: BackupSnapshot)
}

class RoomBackupStore(private val database: AutoCareDatabase) : BackupStore {
  private val profileDao = database.profileDao()
  private val vehicleDao = database.vehicleDao()
  private val maintenanceDao = database.maintenanceDao()

  override suspend fun snapshot(): BackupSnapshot = database.withTransaction {
    BackupSnapshot(
      profiles = profileDao.getAll().map { it.toDomain() },
      vehicles = vehicleDao.getAll().map { it.toDomain() }
    )
  }

  override suspend fun merge(snapshot: BackupSnapshot) {
    val profiles = snapshot.profiles.map { it.toEntity() }
    val vehicles = snapshot.vehicles.map { it.toEntity() }
    val maintenances = snapshot.vehicles.flatMap { vehicle ->
      vehicle.maintenances.orEmpty().map { it.toEntity(vehicle.id!!) }
    }
    // Upsert, nunca REPLACE: o REPLACE apaga a linha antes de reinserir e o
    // ON DELETE CASCADE levaria junto o que esta no aparelho e nao no arquivo.
    // A ordem segue as chaves estrangeiras: perfil, veiculo, manutencao.
    database.withTransaction {
      profileDao.upsertAll(profiles)
      vehicleDao.upsertAll(vehicles)
      maintenanceDao.upsertAll(maintenances)
    }
  }
}

package br.com.geasoftware.autocare.data.backup

import br.com.geasoftware.autocare.data.models.Profile
import br.com.geasoftware.autocare.data.models.Vehicle

/**
 * Tudo o que entra num arquivo de backup: os perfis e os veiculos de todos eles,
 * cada veiculo com as suas manutencoes. As fotos vao a parte, como arquivos.
 */
data class BackupSnapshot(
  val profiles: List<Profile>,
  val vehicles: List<Vehicle>
) {
  /** Caminhos relativos ("images/<uuid>.jpg") referenciados por algum registro. */
  fun photoPaths(): Set<String> = buildSet {
    profiles.mapNotNullTo(this) { it.photoPath }
    vehicles.mapNotNullTo(this) { it.photo }
  }

  val maintenanceCount: Int get() = vehicles.sumOf { it.maintenances.orEmpty().size }
}

/**
 * O que a importacao faria, calculado sem gravar nada. "Atualizado" e um
 * registro que ja existe no aparelho com o mesmo id e com algum campo diferente;
 * o que ja e igual nao conta, para que reimportar o mesmo arquivo diga que nao
 * ha nada a fazer.
 */
data class BackupPreview(
  val newProfiles: Int = 0,
  val updatedProfiles: Int = 0,
  val newVehicles: Int = 0,
  val updatedVehicles: Int = 0,
  val newMaintenances: Int = 0,
  val updatedMaintenances: Int = 0
) {
  val hasNew: Boolean get() = newProfiles + newVehicles + newMaintenances > 0
  val hasUpdated: Boolean get() = updatedProfiles + updatedVehicles + updatedMaintenances > 0
  val isEmpty: Boolean get() = !hasNew && !hasUpdated
}

/** Formato e limites do arquivo. Mudar o formato exige subir [SCHEMA_VERSION]. */
object BackupFormat {
  const val FORMAT = "autocare-backup"
  const val SCHEMA_VERSION = 1
  const val MANIFEST = "manifest.json"
  const val MIME_TYPE = "application/zip"

  /** Nome sugerido ao exportar: autocare-backup-AAAA-MM-DD.zip. */
  fun fileName(date: java.time.LocalDate): String = "$FORMAT-$date.zip"
}

/**
 * Limites da importacao, contra zip bomb. Folga larga para o uso real: uma
 * familia com 20 veiculos e centenas de manutencoes fica abaixo de 1 MB de JSON
 * e de 50 fotos de ~200 KB. Os bytes sao contados no que realmente sai do
 * descompactador - o tamanho declarado na entrada do zip e informado por quem
 * fez o arquivo.
 */
data class BackupLimits(
  val maxEntries: Int = 1_000,
  val maxManifestBytes: Long = 10L * 1024 * 1024,
  val maxImageBytes: Long = 10L * 1024 * 1024,
  val maxTotalBytes: Long = 300L * 1024 * 1024
)

/**
 * Arquivo recusado. A mensagem vai direto para a tela, por isso e em portugues
 * e diz o que a pessoa pode fazer.
 */
sealed class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause) {
  class NotABackup : BackupException("Este arquivo não é um backup do AutoCare")

  class NewerVersion : BackupException("Atualize o app para importar este arquivo")

  class TooLarge : BackupException("O arquivo de backup passa do tamanho permitido")

  class Corrupted(detail: String, cause: Throwable? = null) :
    BackupException("O arquivo de backup está danificado: $detail", cause)
}

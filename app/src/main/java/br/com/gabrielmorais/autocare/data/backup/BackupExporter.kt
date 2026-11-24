package br.com.gabrielmorais.autocare.data.backup

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Gera o .zip de backup com todos os perfis:
 * ```
 * manifest.json
 * images/<uuid>.jpg
 * ```
 *
 * @param imagesRoot a pasta que contem "images/" (o filesDir em producao).
 */
class BackupExporter(
  private val store: BackupStore,
  private val imagesRoot: File,
  private val appVersion: String,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val clock: () -> Instant = Instant::now
) {
  /**
   * Escreve o backup em [output]. Nao fecha [output]: quem abriu o documento
   * fecha.
   *
   * @return o que foi exportado, para a mensagem de sucesso.
   */
  suspend fun export(output: OutputStream): BackupSnapshot = withContext(ioDispatcher) {
    val snapshot = withoutMissingPhotos(store.snapshot())

    val zip = ZipOutputStream(output)
    zip.putNextEntry(ZipEntry(BackupFormat.MANIFEST))
    zip.write(BackupSerializer.encode(snapshot, clock(), appVersion).toByteArray(Charsets.UTF_8))
    zip.closeEntry()

    snapshot.photoPaths().sorted().forEach { path ->
      zip.putNextEntry(ZipEntry(path))
      File(imagesRoot, path).inputStream().use { it.copyTo(zip) }
      zip.closeEntry()
    }
    // finish e nao close: fecha o zip sem fechar o stream de quem chamou.
    zip.finish()
    output.flush()
    snapshot
  }

  /**
   * Uma foto referenciada que nao esta no disco sai do arquivo como "sem foto".
   * Exportar o caminho sem o arquivo faria a importacao apontar para o nada.
   */
  private fun withoutMissingPhotos(snapshot: BackupSnapshot): BackupSnapshot {
    fun String?.ifPresent(): String? = this?.takeIf { File(imagesRoot, it).isFile }
    return snapshot.copy(
      profiles = snapshot.profiles.map { it.copy(photoPath = it.photoPath.ifPresent()) },
      vehicles = snapshot.vehicles.map { it.copy(photo = it.photo.ifPresent()) }
    )
  }
}

package br.com.geasoftware.autocare.data.backup

import br.com.geasoftware.autocare.data.images.ImageKind
import br.com.geasoftware.autocare.data.images.ImagePaths
import br.com.geasoftware.autocare.data.models.Maintenance
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

/**
 * Confere uma foto vinda de um arquivo importado. A implementacao de producao
 * decodifica com o BitmapFactory, que e stub na JVM; nos testes, um fake.
 */
fun interface ImportedImageCheck {
  /**
   * Garante que [file] e um JPEG decodificavel e, se ele passar do lado maximo
   * de [kind], o recomprime no lugar pela politica da etapa 3: um arquivo vindo
   * de fora nao pode furar o limite de espaco das fotos.
   *
   * @throws BackupException.Corrupted se a imagem nao puder ser decodificada.
   */
  fun normalize(file: File, kind: ImageKind)
}

/**
 * Importacao em duas fases, para a previa ser obrigatoria e sem efeito:
 *
 * 1. [prepare] le o zip para uma pasta temporaria no cache, valida tudo e
 *    calcula a [BackupPreview]. Nao toca no banco nem em filesDir/images.
 * 2. [apply] copia as fotos novas e mescla os registros numa unica transacao.
 *    Se algo falhar, as fotos copiadas sao apagadas e o banco nao muda.
 *
 * [discard] (cancelar a previa) apaga a pasta temporaria e nao deixa resto.
 *
 * O arquivo vem de fora: so sao aceitas as entradas "manifest.json" e
 * "images/<uuid>.jpg" (o que tambem impede zip slip), com limites de quantidade
 * e de bytes descompactados contra zip bomb.
 *
 * @param imagesRoot a pasta que contem "images/" (o filesDir em producao).
 * @param stagingRoot onde a importacao em andamento e extraida (no cache).
 */
class BackupImporter(
  private val store: BackupStore,
  private val imagesRoot: File,
  private val stagingRoot: File,
  private val imageCheck: ImportedImageCheck,
  private val limits: BackupLimits = BackupLimits(),
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

  /** Um arquivo lido e validado, a espera da confirmacao. */
  class PreparedImport internal constructor(
    val preview: BackupPreview,
    internal val snapshot: BackupSnapshot,
    internal val stagingDir: File,
    /** Caminho relativo -> arquivo extraido, so das fotos que ainda nao estao no aparelho. */
    internal val newImages: Map<String, File>
  )

  /** @throws BackupException se o arquivo for recusado. */
  suspend fun prepare(input: InputStream): PreparedImport = withContext(ioDispatcher) {
    // So existe uma importacao por vez. O que sobrou de uma anterior (processo
    // morto entre a previa e a confirmacao) e lixo.
    stagingRoot.deleteRecursively()
    val stagingDir = File(stagingRoot, UUID.randomUUID().toString())
    if (!stagingDir.mkdirs()) throw IOException("Não foi possível preparar a importação")

    try {
      val (manifest, extracted) = extract(input, stagingDir)
      val snapshot = resolvePhotos(BackupSerializer.decode(manifest), extracted)
      val newImages = extracted.filterKeys { it in snapshot.photoPaths() }
        .filterKeys { !File(imagesRoot, it).exists() }
      PreparedImport(
        preview = preview(current = store.snapshot(), incoming = snapshot),
        snapshot = snapshot,
        stagingDir = stagingDir,
        newImages = newImages
      )
    } catch (error: Throwable) {
      stagingDir.deleteRecursively()
      throw error
    }
  }

  /**
   * Aplica a importacao confirmada.
   *
   * @return as manutencoes importadas, para cancelar e reagendar os lembretes.
   */
  suspend fun apply(prepared: PreparedImport): List<Maintenance> = withContext(ioDispatcher) {
    val created = mutableListOf<File>()
    try {
      prepared.newImages.forEach { (path, staged) ->
        val target = File(imagesRoot, path)
        // Mesmo uuid = mesma foto: a que ja esta no aparelho fica, e so se apaga
        // no rollback o que esta importacao criou.
        if (target.exists()) return@forEach
        target.parentFile?.mkdirs()
        val partial = File(target.path + ".tmp")
        staged.copyTo(partial, overwrite = true)
        if (!partial.renameTo(target)) {
          partial.delete()
          throw IOException("Não foi possível salvar uma foto importada")
        }
        created += target
      }
      store.merge(prepared.snapshot)
      prepared.snapshot.vehicles.flatMap { it.maintenances.orEmpty() }
    } catch (error: Throwable) {
      created.forEach { it.delete() }
      throw error
    } finally {
      prepared.stagingDir.deleteRecursively()
    }
  }

  /** Cancela a previa: apaga o que foi extraido. */
  suspend fun discard(prepared: PreparedImport) {
    withContext(ioDispatcher) { prepared.stagingDir.deleteRecursively() }
  }

  /** @return o manifest e as fotos extraidas (caminho relativo -> arquivo). */
  private fun extract(input: InputStream, stagingDir: File): Pair<String, Map<String, File>> {
    var manifest: String? = null
    val images = mutableMapOf<String, File>()
    val budget = ByteBudget(limits.maxTotalBytes)
    var entries = 0

    try {
      ZipInputStream(input.buffered()).use { zip ->
        while (true) {
          val entry = zip.nextEntry ?: break
          if (++entries > limits.maxEntries) throw BackupException.TooLarge()
          val name = entry.name
          when {
            // Zip feito a mao por outra ferramenta pode trazer a pasta como entrada.
            entry.isDirectory && name == "${ImagePaths.DIR}/" -> Unit

            name == BackupFormat.MANIFEST -> {
              if (manifest != null) throw BackupException.Corrupted("manifest.json repetido")
              val bytes = ByteArrayOutputStream()
              copyLimited(zip, bytes, limits.maxManifestBytes, budget)
              manifest = bytes.toString(Charsets.UTF_8.name())
            }

            !entry.isDirectory && ImagePaths.isValid(name) -> {
              if (name in images) throw BackupException.Corrupted("foto repetida ($name)")
              val target = File(stagingDir, name)
              // Redundante com o isValid, de proposito: nada sai da pasta temporaria.
              if (!target.canonicalPath.startsWith(stagingDir.canonicalPath + File.separator)) {
                throw BackupException.Corrupted("entrada inválida ($name)")
              }
              target.parentFile?.mkdirs()
              target.outputStream().use { copyLimited(zip, it, limits.maxImageBytes, budget) }
              images[name] = target
            }

            else -> throw BackupException.Corrupted("entrada não permitida ($name)")
          }
          zip.closeEntry()
        }
      }
    } catch (error: ZipException) {
      throw BackupException.Corrupted("o zip não pôde ser lido", error)
    } catch (error: EOFException) {
      // Arquivo cortado no meio (download interrompido, por exemplo).
      throw BackupException.Corrupted("o arquivo está incompleto", error)
    }

    return (manifest ?: throw BackupException.NotABackup()) to images
  }

  /**
   * Confere as fotos referenciadas. A que veio no arquivo precisa ser JPEG
   * valido; a que nao veio so continua referenciada se ja estiver no aparelho
   * (mesmo uuid), senao o registro fica sem foto - melhor do que apontar para
   * um arquivo que nao existe.
   */
  private fun resolvePhotos(snapshot: BackupSnapshot, extracted: Map<String, File>): BackupSnapshot {
    val profilePhotos = snapshot.profiles.mapNotNull { it.photoPath }.toSet()
    snapshot.photoPaths().forEach { path ->
      val file = extracted[path] ?: return@forEach
      if (!ImagePaths.isJpeg(file.readFirstBytes(3))) {
        throw BackupException.Corrupted("a foto $path não é JPEG")
      }
      // Um caminho usado por perfil e por veiculo fica no limite maior.
      val kind = if (path in profilePhotos && snapshot.vehicles.none { it.photo == path }) {
        ImageKind.PROFILE_PHOTO
      } else {
        ImageKind.VEHICLE_PHOTO
      }
      imageCheck.normalize(file, kind)
    }

    fun String?.available(): String? =
      this?.takeIf { it in extracted || File(imagesRoot, it).isFile }
    return snapshot.copy(
      profiles = snapshot.profiles.map { it.copy(photoPath = it.photoPath.available()) },
      vehicles = snapshot.vehicles.map { it.copy(photo = it.photo.available()) }
    )
  }

  private fun preview(current: BackupSnapshot, incoming: BackupSnapshot): BackupPreview {
    val profiles = current.profiles.associateBy { it.id }
    val vehicles = current.vehicles.associateBy { it.id }
    val maintenances = current.vehicles
      .flatMap { vehicle -> vehicle.maintenances.orEmpty().map { it.id to (vehicle.id to it) } }
      .toMap()

    var preview = BackupPreview()
    incoming.profiles.forEach { profile ->
      val existing = profiles[profile.id]
      preview = when {
        existing == null -> preview.copy(newProfiles = preview.newProfiles + 1)
        existing != profile -> preview.copy(updatedProfiles = preview.updatedProfiles + 1)
        else -> preview
      }
    }
    incoming.vehicles.forEach { vehicle ->
      val existing = vehicles[vehicle.id]?.copy(maintenances = null)
      preview = when {
        existing == null -> preview.copy(newVehicles = preview.newVehicles + 1)
        existing != vehicle.copy(maintenances = null) ->
          preview.copy(updatedVehicles = preview.updatedVehicles + 1)
        else -> preview
      }
      vehicle.maintenances.orEmpty().forEach { maintenance ->
        val existingMaintenance = maintenances[maintenance.id]
        preview = when {
          existingMaintenance == null ->
            preview.copy(newMaintenances = preview.newMaintenances + 1)
          // Mudar de veiculo tambem e atualizacao.
          existingMaintenance != (vehicle.id to maintenance) ->
            preview.copy(updatedMaintenances = preview.updatedMaintenances + 1)
          else -> preview
        }
      }
    }
    return preview
  }

  /** Bytes descompactados restantes para o arquivo inteiro. */
  private class ByteBudget(var remaining: Long)

  /**
   * Copia contando os bytes que realmente saem do descompactador: o tamanho
   * declarado na entrada e informado por quem fez o zip e pode mentir.
   */
  private fun copyLimited(input: InputStream, output: OutputStream, limit: Long, budget: ByteBudget) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var copied = 0L
    while (true) {
      val read = input.read(buffer)
      if (read < 0) break
      copied += read
      budget.remaining -= read
      if (copied > limit || budget.remaining < 0) throw BackupException.TooLarge()
      output.write(buffer, 0, read)
    }
  }

  private fun File.readFirstBytes(count: Int): ByteArray =
    inputStream().use { stream -> ByteArray(count).let { it.copyOf(stream.read(it).coerceAtLeast(0)) } }
}

package br.com.gabrielmorais.autocare.data.images

import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Fotos de perfil e de veiculo guardadas no proprio aparelho, em
 * `<filesDir>/images/<uuid>.jpg`. O banco guarda so o caminho relativo
 * ("images/<uuid>.jpg"), que sobrevive a backup e a importacao.
 *
 * Nao ha API para gravar a imagem original: [save] so aceita a saida de
 * [compress], e confere que ela e JPEG antes de gravar.
 *
 * Nao existe "apagar foto". A foto de um veiculo excluido continua no disco
 * enquanto o snackbar oferece desfazer; quem recolhe o que ficou sem dono e
 * [sweepOrphans], na abertura do app.
 *
 * @param compress recebe a origem e devolve bytes JPEG; em producao, o
 * [ImageCompressor]. Injetado para o armazenamento ser testavel na JVM.
 */
class LocalImageStore(
  private val rootDir: File,
  private val cacheDir: File,
  private val compress: (source: Uri, kind: ImageKind) -> ByteArray,
  private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
  private val clock: () -> Long = System::currentTimeMillis
) {
  private val imagesDir = File(rootDir, ImagePaths.DIR)

  /**
   * Comprime [source] conforme [kind], grava e devolve o caminho relativo.
   * Em seguida apaga as copias temporarias que o cropper deixou no cache.
   */
  suspend fun save(source: Uri, kind: ImageKind): String = withContext(ioDispatcher) {
    val bytes = compress(source, kind)
    if (!ImagePaths.isJpeg(bytes)) throw IOException("A imagem comprimida não é JPEG")

    if (!imagesDir.isDirectory && !imagesDir.mkdirs()) {
      throw IOException("Não foi possível criar a pasta de imagens")
    }
    val path = ImagePaths.newPath()
    val target = File(rootDir, path)
    // Grava num .tmp e renomeia: um processo morto no meio da escrita deixa um
    // .tmp (recolhido pela varredura), nunca um .jpg truncado referenciado no banco.
    val partial = File(target.path + ".tmp")
    try {
      partial.writeBytes(bytes)
      if (!partial.renameTo(target)) throw IOException("Não foi possível salvar a imagem")
    } catch (error: IOException) {
      partial.delete()
      throw error
    }

    clearCropperCache()
    path
  }

  /** @throws IllegalArgumentException se [path] nao for um caminho gerado por [save]. */
  fun resolve(path: String): File {
    require(ImagePaths.isValid(path)) { "Caminho de imagem inválido: $path" }
    return File(rootDir, path)
  }

  /**
   * Apaga as imagens que nenhum registro referencia mais (foto trocada, veiculo
   * ou perfil excluido) e sobras de gravacao interrompida.
   *
   * Arquivos com menos de [ImagePaths.ORPHAN_GRACE_MILLIS] sao poupados: uma foto
   * acabou de ser salva e o registro que vai referencia-la pode ainda nao ter
   * sido gravado no banco.
   *
   * @return quantos arquivos foram apagados.
   */
  suspend fun sweepOrphans(referenced: Set<String>): Int = withContext(ioDispatcher) {
    val files = imagesDir.listFiles()?.filter { it.isFile }.orEmpty()
    val toDelete = ImagePaths.orphans(
      files = files.map { ImagePaths.StoredFile(it.name, it.lastModified()) },
      referenced = referenced,
      now = clock()
    )
    toDelete.count { name -> File(imagesDir, name).delete() }
  }

  /**
   * O cropper grava o recorte e a foto da camera direto no cacheDir e nunca os
   * apaga. So os prefixos dele, so na raiz do cache: o resto do cache e de
   * outras bibliotecas (Coil, WorkManager).
   */
  fun clearCropperCache(): Int =
    cacheDir.listFiles()
      ?.filter { it.isFile && ImagePaths.isCropperTemp(it.name) }
      ?.count { it.delete() }
      ?: 0
}

/** Regras de nome e de limpeza, sem I/O, para teste na JVM. */
object ImagePaths {
  const val DIR = "images"

  /** Uma hora: folga larga para a foto salva ganhar o registro no banco. */
  const val ORPHAN_GRACE_MILLIS = 60 * 60 * 1000L

  private val VALID_PATH = Regex("^$DIR/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg$")

  /** Prefixos dos temporarios da biblioteca de recorte (android-image-cropper). */
  private val CROPPER_TEMP_PREFIXES = listOf("cropped", "tmp_image_file")

  data class StoredFile(val name: String, val lastModified: Long)

  fun newPath(): String = "$DIR/${UUID.randomUUID()}.jpg"

  /**
   * So o formato gerado por [newPath]. Recusa "..", caminho absoluto e qualquer
   * outra pasta: o caminho chega do banco e, na etapa 8, de arquivo importado.
   */
  fun isValid(path: String): Boolean = VALID_PATH.matches(path)

  /** Marcador SOI do JPEG seguido do inicio de um segmento: FF D8 FF. */
  fun isJpeg(bytes: ByteArray): Boolean =
    bytes.size >= 3 &&
      bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()

  fun isCropperTemp(fileName: String): Boolean =
    CROPPER_TEMP_PREFIXES.any { fileName.startsWith(it) }

  /** Nomes (dentro de [DIR]) a apagar. Ver [LocalImageStore.sweepOrphans]. */
  fun orphans(files: List<StoredFile>, referenced: Set<String>, now: Long): List<String> =
    files
      .filter { now - it.lastModified >= ORPHAN_GRACE_MILLIS }
      .filter { "$DIR/${it.name}" !in referenced }
      .map { it.name }
}

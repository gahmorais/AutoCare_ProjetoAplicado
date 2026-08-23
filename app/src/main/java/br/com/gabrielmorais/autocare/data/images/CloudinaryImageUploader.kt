package br.com.gabrielmorais.autocare.data.images

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Le o conteudo local escolhido pelo usuario e delega o envio a
 * [CloudinaryUploadApi].
 *
 * Esta classe existe separada do cliente HTTP porque ContentResolver.openInputStream
 * e getType sao final: nao ha como fakea-los em teste JVM. Isolando a leitura aqui,
 * a logica de envio fica testavel e o que sobra e trivial o bastante para ser
 * verificado no aparelho.
 */
class CloudinaryImageUploader(
  private val contentResolver: ContentResolver,
  private val api: CloudinaryUploadApi,
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageUploader {

  override suspend fun upload(image: Uri): String {
    val bytes = withContext(dispatcher) {
      contentResolver.openInputStream(image)?.use { it.readBytes() }
    } ?: throw IOException("Não foi possível ler a imagem selecionada")

    return api.upload(bytes, contentResolver.getType(image))
  }
}

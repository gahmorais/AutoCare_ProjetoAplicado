package br.com.gabrielmorais.autocare.data.images

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Comprime o conteudo local escolhido pelo usuario e delega o envio a
 * [CloudinaryUploadApi].
 *
 * Esta classe existe separada do cliente HTTP porque ContentResolver e
 * BitmapFactory sao final ou stubs em teste JVM. Isolando o lado Android aqui, a
 * logica de envio fica testavel e o que sobra e fino o bastante para ser
 * verificado no aparelho.
 */
class CloudinaryImageUploader(
  private val contentResolver: ContentResolver,
  private val api: CloudinaryUploadApi,
  private val compressor: ImageCompressor = ImageCompressor(),
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ImageUploader {

  override suspend fun upload(image: Uri): String {
    val bytes = withContext(dispatcher) { compressor.compress(contentResolver, image) }
    return api.upload(bytes, ImageCompressor.OUTPUT_MIME_TYPE)
  }
}

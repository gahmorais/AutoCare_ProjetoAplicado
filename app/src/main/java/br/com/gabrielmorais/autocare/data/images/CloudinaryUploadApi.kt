package br.com.gabrielmorais.autocare.data.images

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Cliente do endpoint de upload nao assinado do Cloudinary.
 *
 * Nao assinado porque a alternativa exigiria o api_secret no cliente, o que e
 * inaceitavel, ou um backend para assinar, que o projeto nao tem. A consequencia
 * assumida: quem descompilar o APK consegue subir imagens nesta conta. As
 * restricoes que limitam o estrago (formatos, tamanho, dimensao, pasta) vivem na
 * configuracao do preset, no console do Cloudinary.
 *
 * Sem nenhum tipo do Android na assinatura, de proposito: assim o trecho que
 * concentra o risco fica coberto por teste unitario sem Robolectric.
 */
class CloudinaryUploadApi(
  private val client: OkHttpClient,
  private val cloudName: String,
  private val uploadPreset: String,
  baseUrl: HttpUrl = DEFAULT_BASE_URL.toHttpUrl(),
  private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

  private val uploadUrl: HttpUrl = baseUrl.newBuilder()
    .addPathSegments("v1_1/$cloudName/image/upload")
    .build()

  /** @return a `secure_url` do asset criado. */
  suspend fun upload(bytes: ByteArray, mimeType: String?): String = withContext(dispatcher) {
    check(cloudName.isNotBlank() && uploadPreset.isNotBlank()) {
      "Cloudinary não configurado: defina cloudinary.cloudName e " +
        "cloudinary.uploadPreset em local.properties"
    }
    if (bytes.isEmpty()) throw IOException("A imagem selecionada está vazia")

    val mediaType = mimeType?.toMediaTypeOrNull() ?: FALLBACK_MEDIA_TYPE.toMediaTypeOrNull()

    val body = MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("upload_preset", uploadPreset)
      // Sem public_id de proposito: quem define o nome do asset e o Cloudinary.
      // Se o cliente pudesse escolher, bastaria informar o id de outra pessoa
      // para sobrescrever a imagem dela.
      .addFormDataPart("file", FILE_PART_NAME, bytes.toRequestBody(mediaType))
      .build()

    val request = Request.Builder().url(uploadUrl).post(body).build()

    client.newCall(request).execute().use { response ->
      val payload = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        throw IOException(errorMessage(payload, response.code))
      }

      val secureUrl = runCatching { JSONObject(payload).optString(FIELD_SECURE_URL) }.getOrNull()
      if (secureUrl.isNullOrBlank()) {
        throw IOException("Resposta do Cloudinary sem $FIELD_SECURE_URL")
      }
      secureUrl
    }
  }

  /** O Cloudinary responde erro como `{"error":{"message":"..."}}`. */
  private fun errorMessage(payload: String, code: Int): String =
    runCatching { JSONObject(payload).getJSONObject("error").getString("message") }
      .getOrNull()
      ?.takeIf { it.isNotBlank() }
      ?.let { "Falha no envio da imagem: $it" }
      ?: "Falha no envio da imagem (HTTP $code)"

  companion object {
    const val DEFAULT_BASE_URL = "https://api.cloudinary.com/"
    private const val FALLBACK_MEDIA_TYPE = "image/jpeg"
    private const val FILE_PART_NAME = "upload"
    private const val FIELD_SECURE_URL = "secure_url"
  }
}

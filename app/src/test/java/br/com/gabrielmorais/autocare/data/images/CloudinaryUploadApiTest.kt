package br.com.gabrielmorais.autocare.data.images

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
internal class CloudinaryUploadApiTest {

  private lateinit var server: MockWebServer

  private val imageBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 1, 2, 3)

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  private fun api(
    cloudName: String = "cloud-teste",
    uploadPreset: String = "preset-teste"
  ) = CloudinaryUploadApi(
    client = OkHttpClient(),
    cloudName = cloudName,
    uploadPreset = uploadPreset,
    baseUrl = server.url("/"),
    dispatcher = UnconfinedTestDispatcher()
  )

  @Test
  fun `devolve a secure_url da resposta`() = runTest {
    server.enqueue(
      MockResponse().setBody(
        """{"public_id":"autocare/abc","secure_url":"https://res.cloudinary.com/c/image/upload/v1/abc.jpg"}"""
      )
    )

    val url = api().upload(imageBytes, "image/jpeg")

    assertEquals("https://res.cloudinary.com/c/image/upload/v1/abc.jpg", url)
  }

  @Test
  fun `monta o endpoint de upload com o cloud name`() = runTest {
    server.enqueue(MockResponse().setBody("""{"secure_url":"https://x/y.jpg"}"""))

    api(cloudName = "meu-cloud").upload(imageBytes, "image/jpeg")

    val request = server.takeRequest()
    assertEquals("POST", request.method)
    assertEquals("/v1_1/meu-cloud/image/upload", request.path)
  }

  @Test
  fun `envia o upload preset e o arquivo no multipart`() = runTest {
    server.enqueue(MockResponse().setBody("""{"secure_url":"https://x/y.jpg"}"""))

    api(uploadPreset = "autocare-preset").upload(imageBytes, "image/jpeg")

    val body = server.takeRequest().body.readUtf8()
    assertTrue(body.contains("name=\"upload_preset\""))
    assertTrue(body.contains("autocare-preset"))
    assertTrue(body.contains("name=\"file\""))
  }

  // Deixar o Cloudinary gerar o public_id e o que impede um cliente de
  // sobrescrever a imagem de outra pessoa.
  @Test
  fun `nao envia public_id`() = runTest {
    server.enqueue(MockResponse().setBody("""{"secure_url":"https://x/y.jpg"}"""))

    api().upload(imageBytes, "image/jpeg")

    assertTrue(!server.takeRequest().body.readUtf8().contains("name=\"public_id\""))
  }

  @Test
  fun `propaga a mensagem de erro do cloudinary`() = runTest {
    server.enqueue(
      MockResponse()
        .setResponseCode(400)
        .setBody("""{"error":{"message":"Upload preset not found"}}""")
    )

    val error = runCatching { api().upload(imageBytes, "image/jpeg") }.exceptionOrNull()

    assertTrue(error is IOException)
    assertEquals("Falha no envio da imagem: Upload preset not found", error?.message)
  }

  @Test
  fun `usa o codigo http quando o corpo de erro nao e json`() = runTest {
    server.enqueue(MockResponse().setResponseCode(500).setBody("<html>oops</html>"))

    val error = runCatching { api().upload(imageBytes, "image/jpeg") }.exceptionOrNull()

    assertEquals("Falha no envio da imagem (HTTP 500)", error?.message)
  }

  @Test
  fun `falha quando a resposta nao traz secure_url`() = runTest {
    server.enqueue(MockResponse().setBody("""{"public_id":"autocare/abc"}"""))

    val error = runCatching { api().upload(imageBytes, "image/jpeg") }.exceptionOrNull()

    assertTrue(error is IOException)
    assertEquals("Resposta do Cloudinary sem secure_url", error?.message)
  }

  @Test
  fun `falha quando a imagem esta vazia sem chamar a rede`() = runTest {
    val error = runCatching { api().upload(ByteArray(0), "image/jpeg") }.exceptionOrNull()

    assertTrue(error is IOException)
    assertEquals(0, server.requestCount)
  }

  @Test
  fun `falha com mensagem clara quando o preset nao esta configurado`() = runTest {
    val error = runCatching {
      api(uploadPreset = "").upload(imageBytes, "image/jpeg")
    }.exceptionOrNull()

    assertTrue(error is IllegalStateException)
    assertTrue(error?.message.orEmpty().contains("local.properties"))
    assertEquals(0, server.requestCount)
  }
}

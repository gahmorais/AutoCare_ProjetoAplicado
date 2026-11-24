package br.com.gabrielmorais.autocare.data.images

import android.net.Uri
import br.com.gabrielmorais.autocare.data.images.ImagePaths.ORPHAN_GRACE_MILLIS
import br.com.gabrielmorais.autocare.data.images.ImagePaths.StoredFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import java.io.File
import java.io.IOException

private val JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 1, 2, 3)
private val PNG = byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())

class ImagePathsTest {

  @Test
  fun `caminho novo e valido e fica dentro de images`() {
    val path = ImagePaths.newPath()
    assertTrue(path, ImagePaths.isValid(path))
    assertTrue(path.startsWith("images/") && path.endsWith(".jpg"))
  }

  @Test
  fun `caminhos fora do formato sao recusados`() {
    listOf(
      "../images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg",
      "images/../shared_prefs/x.xml",
      "/data/data/app/files/images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg",
      "images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.png",
      "images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg.tmp",
      "images/sub/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg",
      "outra/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg",
      "images/foto.jpg",
      ""
    ).forEach { assertFalse(it, ImagePaths.isValid(it)) }
  }

  @Test
  fun `reconhece o marcador do jpeg`() {
    assertTrue(ImagePaths.isJpeg(JPEG))
    assertFalse(ImagePaths.isJpeg(PNG))
    assertFalse(ImagePaths.isJpeg(byteArrayOf(0xFF.toByte(), 0xD8.toByte())))
    assertFalse(ImagePaths.isJpeg(ByteArray(0)))
  }

  @Test
  fun `orfaos sao os nao referenciados e mais velhos que a carencia`() {
    val now = 10 * ORPHAN_GRACE_MILLIS
    val old = now - ORPHAN_GRACE_MILLIS
    val files = listOf(
      StoredFile("usada.jpg", old),
      StoredFile("sem-dono.jpg", old),
      StoredFile("recem-salva.jpg", now - 1_000),
      StoredFile("interrompida.jpg.tmp", old)
    )

    val orphans = ImagePaths.orphans(files, referenced = setOf("images/usada.jpg"), now = now)

    assertEquals(listOf("sem-dono.jpg", "interrompida.jpg.tmp"), orphans)
  }

  @Test
  fun `prefixos do cropper`() {
    assertTrue(ImagePaths.isCropperTemp("cropped123456.jpg"))
    assertTrue(ImagePaths.isCropperTemp("tmp_image_file98765.png"))
    assertFalse(ImagePaths.isCropperTemp("image_cache"))
    assertFalse(ImagePaths.isCropperTemp("WorkManager.db"))
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LocalImageStoreTest {

  @get:Rule
  val tmp = TemporaryFolder()

  private lateinit var root: File
  private lateinit var cache: File
  private var now = 100 * ORPHAN_GRACE_MILLIS
  private val source: Uri = mock(Uri::class.java)

  @Before
  fun setUp() {
    root = tmp.newFolder("files")
    cache = tmp.newFolder("cache")
  }

  private fun store(
    dispatcher: kotlinx.coroutines.CoroutineDispatcher,
    compress: (Uri, ImageKind) -> ByteArray = { _, _ -> JPEG }
  ) = LocalImageStore(root, cache, compress, dispatcher, clock = { now })

  @Test
  fun `salva o jpeg comprimido e devolve caminho relativo valido`() = runTest {
    var usedKind: ImageKind? = null
    val store = store(UnconfinedTestDispatcher(testScheduler)) { _, kind -> usedKind = kind; JPEG }

    val path = store.save(source, ImageKind.PROFILE_PHOTO)

    assertTrue(ImagePaths.isValid(path))
    assertEquals(ImageKind.PROFILE_PHOTO, usedKind)
    assertTrue(JPEG.contentEquals(store.resolve(path).readBytes()))
    assertEquals(listOf(path.substringAfter("/")), File(root, "images").list()!!.toList())
  }

  @Test
  fun `recusa gravar o que nao for jpeg`() = runTest {
    val store = store(UnconfinedTestDispatcher(testScheduler)) { _, _ -> PNG }

    try {
      store.save(source, ImageKind.VEHICLE_PHOTO)
      throw AssertionError("deveria recusar bytes que não são JPEG")
    } catch (expected: IOException) {
    }
    assertTrue(File(root, "images").list().orEmpty().isEmpty())
  }

  @Test
  fun `salvar apaga os temporarios do cropper e preserva o resto do cache`() = runTest {
    File(cache, "cropped123.jpg").writeText("x")
    File(cache, "tmp_image_file456.png").writeText("x")
    File(cache, "image_cache").mkdirs()
    File(cache, "outro.bin").writeText("x")

    store(UnconfinedTestDispatcher(testScheduler)).save(source, ImageKind.VEHICLE_PHOTO)

    assertEquals(setOf("image_cache", "outro.bin"), cache.list()!!.toSet())
  }

  @Test
  fun `varredura apaga so orfaos antigos`() = runTest {
    val store = store(UnconfinedTestDispatcher(testScheduler))
    val kept = store.save(source, ImageKind.VEHICLE_PHOTO)
    val orphan = store.save(source, ImageKind.VEHICLE_PHOTO)
    val fresh = store.save(source, ImageKind.VEHICLE_PHOTO)
    listOf(kept, orphan).forEach { store.resolve(it).setLastModified(now - ORPHAN_GRACE_MILLIS) }
    store.resolve(fresh).setLastModified(now - 1_000)

    val deleted = store.sweepOrphans(referenced = setOf(kept))

    assertEquals(1, deleted)
    assertTrue(store.resolve(kept).exists())
    assertFalse(store.resolve(orphan).exists())
    assertTrue("foto recém-salva ainda sem registro", store.resolve(fresh).exists())
  }

  @Test
  fun `varredura nao sai da pasta images`() = runTest {
    val outside = File(root, "shared_prefs").apply { mkdirs() }
    File(outside, "prefs.xml").apply { writeText("x"); setLastModified(0) }
    File(root, "solto.jpg").apply { writeText("x"); setLastModified(0) }

    store(UnconfinedTestDispatcher(testScheduler)).sweepOrphans(referenced = emptySet())

    assertTrue(File(outside, "prefs.xml").exists())
    assertTrue(File(root, "solto.jpg").exists())
  }

  @Test
  fun `varredura sem pasta de imagens nao falha`() = runTest {
    assertEquals(0, store(UnconfinedTestDispatcher(testScheduler)).sweepOrphans(emptySet()))
  }

  @Test
  fun `resolve recusa caminho fora do formato`() = runTest {
    val store = store(UnconfinedTestDispatcher(testScheduler))
    assertThrows(IllegalArgumentException::class.java) { store.resolve("../shared_prefs/x.xml") }
  }
}

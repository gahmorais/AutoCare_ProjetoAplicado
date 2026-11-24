package br.com.gabrielmorais.autocare.data.images

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.R
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.random.Random

/**
 * Instrumentado de proposito: em teste JVM, BitmapFactory e Bitmap sao stubs do
 * android.jar e qualquer assercao passa sem exercitar nada. Foi justamente essa
 * lacuna que deixou passar um `?:` encadeado no retorno de decodeStream com
 * inJustDecodeBounds - que devolve null por contrato e fazia toda compressao
 * falhar com "Não foi possível ler a imagem selecionada".
 */
@RunWith(AndroidJUnit4::class)
class ImageCompressorTest {

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private val contentResolver = context.contentResolver

  private val workDir = File(context.cacheDir, "image-tests").apply { mkdirs() }

  @After
  fun tearDown() {
    workDir.deleteRecursively()
  }

  private fun drawableUri(resId: Int): Uri =
    Uri.parse("android.resource://${context.packageName}/$resId")

  /**
   * Foto sintetica do tamanho de uma camera de celular (4000x3000): degrade e
   * formas com semente fixa, para o tamanho do resultado ser reprodutivel.
   * Gravada em JPEG 95 com o EXIF pedido, como sairia da camera.
   */
  private fun cameraPhoto(
    width: Int = 4000,
    height: Int = 3000,
    orientation: Int = ExifInterface.ORIENTATION_NORMAL,
    withGps: Boolean = false
  ): File {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(
      0f, 0f, width.toFloat(), height.toFloat(),
      Color.rgb(30, 90, 160), Color.rgb(230, 200, 120), Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null
    val random = Random(42)
    repeat(300) {
      paint.color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256))
      canvas.drawCircle(
        random.nextInt(width).toFloat(), random.nextInt(height).toFloat(),
        (20 + random.nextInt(width / 20)).toFloat(), paint
      )
    }
    return writeJpeg(bitmap, orientation, withGps)
  }

  private fun writeJpeg(bitmap: Bitmap, orientation: Int, withGps: Boolean): File {
    val file = File.createTempFile("photo", ".jpg", workDir)
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
    bitmap.recycle()
    ExifInterface(file).apply {
      setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
      if (withGps) setLatLong(-23.5505, -46.6333)
      saveAttributes()
    }
    return file
  }

  private fun compress(kind: ImageKind, file: File): ByteArray =
    ImageCompressor(kind).compress(contentResolver, Uri.fromFile(file))

  private fun decode(bytes: ByteArray): Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

  @Test
  fun foto_de_veiculo_sai_jpeg_reduzida_e_leve() {
    val original = cameraPhoto()

    val bytes = compress(ImageKind.VEHICLE_PHOTO, original)
    val decoded = decode(bytes)

    assertTrue("deveria ser JPEG", ImagePaths.isJpeg(bytes))
    assertEquals(1280, maxOf(decoded.width, decoded.height))
    assertEquals(4f / 3f, decoded.width.toFloat() / decoded.height, 0.01f)
    assertTrue("${bytes.size} bytes, teto de 400 KB", bytes.size < 400 * 1024)
    assertTrue("deveria ficar menor que o original", bytes.size < original.length())
  }

  @Test
  fun foto_de_perfil_sai_jpeg_reduzida_e_leve() {
    val bytes = compress(ImageKind.PROFILE_PHOTO, cameraPhoto())
    val decoded = decode(bytes)

    assertTrue("deveria ser JPEG", ImagePaths.isJpeg(bytes))
    assertEquals(512, maxOf(decoded.width, decoded.height))
    assertTrue("${bytes.size} bytes, teto de 100 KB", bytes.size < 100 * 1024)
  }

  @Test
  fun imagem_menor_que_o_limite_nao_e_ampliada() {
    val bytes = compress(ImageKind.VEHICLE_PHOTO, cameraPhoto(width = 800, height = 600))
    val decoded = decode(bytes)

    assertEquals(800, decoded.width)
    assertEquals(600, decoded.height)
  }

  @Test
  fun orientacao_do_exif_e_aplicada_na_imagem() {
    // Metade esquerda vermelha, direita azul, deitada (400x200). O EXIF manda
    // girar 90 graus no sentido horario: a borda esquerda vira o topo.
    val bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
    Canvas(bitmap).apply {
      drawRect(0f, 0f, 200f, 200f, Paint().apply { color = Color.RED })
      drawRect(200f, 0f, 400f, 200f, Paint().apply { color = Color.BLUE })
    }
    val file = writeJpeg(bitmap, ExifInterface.ORIENTATION_ROTATE_90, withGps = false)

    val decoded = decode(compress(ImageKind.VEHICLE_PHOTO, file))

    assertEquals("deveria sair em retrato", 200, decoded.width)
    assertEquals(400, decoded.height)
    val top = decoded.getPixel(100, 50)
    val bottom = decoded.getPixel(100, 350)
    assertTrue("topo deveria ser vermelho", Color.red(top) > 200 && Color.blue(top) < 60)
    assertTrue("base deveria ser azul", Color.blue(bottom) > 200 && Color.red(bottom) < 60)
  }

  @Test
  fun exif_da_origem_inclusive_gps_e_descartado() {
    val original = cameraPhoto(orientation = ExifInterface.ORIENTATION_ROTATE_90, withGps = true)
    assertTrue("pré-condição: origem com GPS", ExifInterface(original).latLong != null)

    val exif = ExifInterface(ByteArrayInputStream(compress(ImageKind.VEHICLE_PHOTO, original)))

    assertNull("a localização não pode sair do aparelho na foto", exif.latLong)
    assertEquals(0, exif.rotationDegrees)
  }

  @Test
  fun armazenamento_grava_a_foto_comprimida_e_limpa_o_cache_do_cropper() = runBlocking {
    val root = File(workDir, "files").apply { mkdirs() }
    val cache = File(workDir, "cache").apply { mkdirs() }
    val cropped = cameraPhoto().copyTo(File(cache, "cropped12345.jpg"))
    val store = LocalImageStore(
      rootDir = root,
      cacheDir = cache,
      compress = { source, kind -> ImageCompressor(kind).compress(contentResolver, source) }
    )

    val path = store.save(Uri.fromFile(cropped), ImageKind.VEHICLE_PHOTO)

    val saved = store.resolve(path)
    assertTrue(saved.exists())
    assertTrue(ImagePaths.isJpeg(saved.readBytes()))
    assertEquals(1280, maxOf(decode(saved.readBytes()).width, decode(saved.readBytes()).height))
    assertFalse("o recorte temporário deveria ter sido apagado", cropped.exists())
  }

  @Test
  fun comprime_imagem_real_sem_lancar() {
    val bytes = ImageCompressor().compress(contentResolver, drawableUri(R.drawable.car_photo))

    assertTrue("a compressão deveria produzir bytes", bytes.isNotEmpty())
  }

  @Test
  fun resultado_e_um_jpeg_decodificavel() {
    val bytes = ImageCompressor().compress(contentResolver, drawableUri(R.drawable.car_photo))

    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    assertTrue("os bytes deveriam ser uma imagem válida", decoded != null)
  }

  @Test
  fun respeita_a_dimensao_maxima() {
    val maxDimension = 64
    val bytes = ImageCompressor(maxDimension = maxDimension)
      .compress(contentResolver, drawableUri(R.drawable.car_photo))

    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    assertTrue(
      "maior lado deveria ser <= $maxDimension, foi ${decoded.width}x${decoded.height}",
      maxOf(decoded.width, decoded.height) <= maxDimension
    )
  }

  @Test
  fun preserva_a_proporcao_ao_reduzir() {
    val original = BitmapFactory.decodeResource(context.resources, R.drawable.car_photo)
    val originalRatio = original.width.toFloat() / original.height

    val bytes = ImageCompressor(maxDimension = 100)
      .compress(contentResolver, drawableUri(R.drawable.car_photo))
    val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    assertEquals(originalRatio, decoded.width.toFloat() / decoded.height, 0.05f)
  }

  @Test(expected = Exception::class)
  fun falha_quando_o_uri_nao_existe() {
    ImageCompressor().compress(
      contentResolver,
      Uri.parse("android.resource://${context.packageName}/0")
    )
  }
}

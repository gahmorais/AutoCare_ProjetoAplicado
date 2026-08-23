package br.com.gabrielmorais.autocare.data.images

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

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

  private fun drawableUri(resId: Int): Uri =
    Uri.parse("android.resource://${context.packageName}/$resId")

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

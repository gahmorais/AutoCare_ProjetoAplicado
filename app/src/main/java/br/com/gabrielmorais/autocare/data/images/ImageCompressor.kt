package br.com.gabrielmorais.autocare.data.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Reduz a imagem antes do envio.
 *
 * Antes o app subia o bitmap cru saido do cropper - alguns MB por foto. Alem do
 * custo de banda, era a principal causa de upload lento ou falho em rede movel.
 *
 * Nao ha teste unitario aqui: BitmapFactory e Bitmap sao stubs no android.jar de
 * teste e qualquer assercao passaria sem exercitar nada. A logica foi mantida
 * curta de proposito; a verificacao e no aparelho.
 */
class ImageCompressor(
  private val maxDimension: Int = DEFAULT_MAX_DIMENSION,
  private val quality: Int = DEFAULT_QUALITY
) {

  /** @return bytes JPEG prontos para envio. */
  fun compress(contentResolver: ContentResolver, image: Uri): ByteArray {
    // Atencao: com inJustDecodeBounds, decodeStream devolve null por contrato e
    // so preenche outWidth/outHeight. Encadear um ?: no retorno dele - como esta
    // versao fazia - falha para toda imagem, sempre. A checagem de nulo pertence
    // ao openInputStream, nao ao decode.
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openStream(contentResolver, image).use { BitmapFactory.decodeStream(it, null, bounds) }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
      throw IOException("O arquivo selecionado não é uma imagem válida")
    }

    // Primeiro corte, barato: o decoder ja devolve a imagem reduzida por potencia
    // de 2, sem alocar o bitmap em tamanho original.
    val options = BitmapFactory.Options().apply {
      inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight)
    }
    val decoded = openStream(contentResolver, image)
      .use { BitmapFactory.decodeStream(it, null, options) }
      ?: throw IOException("Não foi possível decodificar a imagem selecionada")

    val scaled = scaleWithinBounds(decoded)

    return try {
      ByteArrayOutputStream().use { output ->
        // Sempre JPEG: as fotos de perfil e de veiculo sao opacas e saem do
        // cropper sem transparencia, entao nao ha canal alfa a perder.
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        output.toByteArray()
      }
    } finally {
      if (scaled !== decoded) scaled.recycle()
      decoded.recycle()
    }
  }

  private fun openStream(contentResolver: ContentResolver, image: Uri): InputStream =
    contentResolver.openInputStream(image)
      ?: throw IOException("Não foi possível ler a imagem selecionada")

  private fun calculateInSampleSize(width: Int, height: Int): Int {
    var sampleSize = 1
    while (width / (sampleSize * 2) >= maxDimension || height / (sampleSize * 2) >= maxDimension) {
      sampleSize *= 2
    }
    return sampleSize
  }

  /** Ajuste fino apos o inSampleSize, que so reduz em potencias de 2. */
  private fun scaleWithinBounds(bitmap: Bitmap): Bitmap {
    val largestSide = maxOf(bitmap.width, bitmap.height)
    if (largestSide <= maxDimension) return bitmap

    val ratio = maxDimension.toFloat() / largestSide
    val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
    val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
  }

  companion object {
    const val DEFAULT_MAX_DIMENSION = 1600
    const val DEFAULT_QUALITY = 85
    const val OUTPUT_MIME_TYPE = "image/jpeg"
  }
}

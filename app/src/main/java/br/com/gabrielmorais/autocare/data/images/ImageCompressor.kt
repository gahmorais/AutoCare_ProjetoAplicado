package br.com.gabrielmorais.autocare.data.images

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Politica de imagem do app: toda foto salva passa por aqui, sempre em JPEG e
 * redimensionada. Ver a etapa 3 de docs/plano-publicacao.md.
 *
 * Qualidade 75 (faixa 70-80): sem perda visivel em tela de celular e perto da
 * metade dos bytes do 85 usado antes. Para economizar espaco, reduza o lado
 * maximo - nao a qualidade abaixo de 70.
 */
enum class ImageKind(val maxDimension: Int, val quality: Int) {
  /** Card e detalhe do veiculo nunca exibem acima de 1280 px. */
  VEHICLE_PHOTO(maxDimension = 1280, quality = 75),

  /** O avatar aparece com no maximo ~120 dp. */
  PROFILE_PHOTO(maxDimension = 512, quality = 75)
}

/**
 * Reduz e recodifica a imagem em JPEG.
 *
 * Recodificar tambem descarta todo o EXIF da origem - inclusive a localizacao
 * GPS de onde a foto foi tirada. A orientacao do EXIF e aplicada no bitmap
 * antes, senao a foto de camera em retrato sairia deitada.
 *
 * Nao ha teste unitario aqui: BitmapFactory e Bitmap sao stubs no android.jar de
 * teste e qualquer assercao passaria sem exercitar nada. A verificacao e no
 * aparelho (ImageCompressorTest, instrumentado).
 */
class ImageCompressor(
  private val maxDimension: Int = ImageKind.VEHICLE_PHOTO.maxDimension,
  private val quality: Int = ImageKind.VEHICLE_PHOTO.quality
) {
  constructor(kind: ImageKind) : this(kind.maxDimension, kind.quality)

  /** @return bytes JPEG. */
  fun compress(contentResolver: ContentResolver, image: Uri): ByteArray {
    // Atencao: com inJustDecodeBounds, decodeStream devolve null por contrato e
    // so preenche outWidth/outHeight. Encadear um ?: no retorno dele - como uma
    // versao anterior fazia - falha para toda imagem, sempre. A checagem de nulo
    // pertence ao openInputStream, nao ao decode.
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
    val oriented = applyOrientation(scaled, readOrientation(contentResolver, image))

    return try {
      ByteArrayOutputStream().use { output ->
        // Sempre JPEG: as fotos de perfil e de veiculo sao opacas e saem do
        // cropper sem transparencia, entao nao ha canal alfa a perder.
        oriented.compress(Bitmap.CompressFormat.JPEG, quality, output)
        output.toByteArray()
      }
    } finally {
      listOf(oriented, scaled, decoded).distinct().forEach { it.recycle() }
    }
  }

  private fun openStream(contentResolver: ContentResolver, image: Uri): InputStream =
    contentResolver.openInputStream(image)
      ?: throw IOException("Não foi possível ler a imagem selecionada")

  /**
   * Sem EXIF legivel (PNG, recurso do app, arquivo sem metadados) a imagem e
   * tratada como ja orientada - nao e motivo para recusar a foto.
   */
  private fun readOrientation(contentResolver: ContentResolver, image: Uri): ExifInterface? =
    runCatching { openStream(contentResolver, image).use { ExifInterface(it) } }.getOrNull()

  private fun applyOrientation(bitmap: Bitmap, exif: ExifInterface?): Bitmap {
    if (exif == null) return bitmap
    val degrees = exif.rotationDegrees
    val flipped = exif.isFlipped
    if (degrees == 0 && !flipped) return bitmap

    // rotationDegrees e definido como a rotacao a aplicar depois do espelhamento.
    val matrix = Matrix().apply {
      if (flipped) postScale(-1f, 1f)
      postRotate(degrees.toFloat())
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
  }

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
    const val OUTPUT_MIME_TYPE = "image/jpeg"
  }
}

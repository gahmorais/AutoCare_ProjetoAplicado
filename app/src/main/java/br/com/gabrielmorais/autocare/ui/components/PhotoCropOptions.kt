package br.com.gabrielmorais.autocare.ui.components

import android.graphics.Bitmap
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

/**
 * Opcoes do recorte num lugar so, para as telas nao divergirem.
 *
 * O recorte e so um intermediario no cacheDir: ele e recomprimido pelo
 * ImageCompressor (JPEG 75, lado maximo por tipo) e apagado logo depois de salvo.
 * Por isso a qualidade aqui e mais alta (90) - recomprimir um JPEG ja degradado
 * somaria as perdas das duas passagens.
 */
object PhotoCropOptions {
  private const val INTERMEDIATE_QUALITY = 90

  /**
   * Proporcao 2:1, a do card do veiculo.
   *
   * @param includeCamera false quando a permissao da camera foi negada: o
   * intent de captura falharia, mas a galeria continua valendo.
   */
  fun vehicle(includeCamera: Boolean = true): CropImageContractOptions =
    options(aspectRatioX = 2, aspectRatioY = 1, includeCamera = includeCamera)

  /** Quadrada, para o avatar redondo. Ver [vehicle] sobre [includeCamera]. */
  fun profile(includeCamera: Boolean = true): CropImageContractOptions =
    options(aspectRatioX = 1, aspectRatioY = 1, includeCamera = includeCamera)

  private fun options(aspectRatioX: Int, aspectRatioY: Int, includeCamera: Boolean) = CropImageContractOptions(
    null,
    CropImageOptions(
      imageSourceIncludeGallery = true,
      imageSourceIncludeCamera = includeCamera,
      guidelines = CropImageView.Guidelines.ON,
      // Sem fixAspectRatio a proporcao era so a inicial e o usuario podia
      // recortar livre - a foto do veiculo saia quadrada num banner 2:1.
      fixAspectRatio = true,
      aspectRatioX = aspectRatioX,
      aspectRatioY = aspectRatioY,
      outputCompressFormat = Bitmap.CompressFormat.JPEG,
      outputCompressQuality = INTERMEDIATE_QUALITY
    )
  )
}

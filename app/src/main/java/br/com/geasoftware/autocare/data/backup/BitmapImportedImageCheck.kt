package br.com.geasoftware.autocare.data.backup

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import br.com.geasoftware.autocare.data.images.ImageCompressor
import br.com.geasoftware.autocare.data.images.ImageKind
import br.com.geasoftware.autocare.data.images.ImagePaths
import java.io.File

/**
 * [ImportedImageCheck] de producao. Coberto pelo teste instrumentado: o
 * BitmapFactory e stub na JVM.
 */
class BitmapImportedImageCheck(private val contentResolver: ContentResolver) : ImportedImageCheck {

  override fun normalize(file: File, kind: ImageKind) {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) invalid(file)

    // Os limites do cabecalho nao provam que os dados da imagem decodificam.
    // Uma decodificacao bem reduzida prova, sem alocar a foto em tamanho cheio.
    val probe = BitmapFactory.Options().apply {
      inSampleSize = Integer.highestOneBit(maxOf(bounds.outWidth, bounds.outHeight) / PROBE_SIZE)
        .coerceAtLeast(1)
    }
    val decoded = BitmapFactory.decodeFile(file.path, probe) ?: invalid(file)
    decoded.recycle()

    if (maxOf(bounds.outWidth, bounds.outHeight) > kind.maxDimension) {
      val bytes = ImageCompressor(kind).compress(contentResolver, Uri.fromFile(file))
      if (!ImagePaths.isJpeg(bytes)) invalid(file)
      file.writeBytes(bytes)
    }
  }

  private fun invalid(file: File): Nothing =
    throw BackupException.Corrupted("a foto ${ImagePaths.DIR}/${file.name} não pôde ser aberta")

  private companion object {
    const val PROBE_SIZE = 64
  }
}

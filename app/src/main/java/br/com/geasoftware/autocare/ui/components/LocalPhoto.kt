package br.com.geasoftware.autocare.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import br.com.geasoftware.autocare.data.images.ImagePaths
import java.io.File

/**
 * Arquivo da foto para o Coil carregar, a partir do caminho relativo gravado no
 * banco. Devolve null sem foto ou com caminho fora do formato (ex.: URL antiga
 * de servidor), e a tela cai no seu placeholder.
 */
@Composable
fun rememberLocalPhoto(path: String?): File? {
  val filesDir = LocalContext.current.filesDir
  return remember(path, filesDir) {
    path?.takeIf(ImagePaths::isValid)?.let { File(filesDir, it) }
  }
}

package br.com.gabrielmorais.autocare.ui.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract

/**
 * Avatar redondo que, ao toque, abre camera/galeria e o recorte quadrado.
 * Usado no primeiro acesso e na tela de Perfil.
 *
 * @param photo o que o Coil deve exibir: File da foto salva, Uri do recorte
 * ainda nao salvo, ou null para o icone de pessoa.
 */
@Composable
fun ProfileAvatarPicker(
  photo: Any?,
  onPhotoPicked: (Uri) -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 112.dp
) {
  val cropImage = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result -> result.uriContent?.let(onPhotoPicked) }
  )
  // O manifesto declara CAMERA, entao o intent de captura exige a permissao
  // concedida mesmo sendo o cropper quem abre a camera.
  val requestCamera = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted -> if (granted) cropImage.launch(PhotoCropOptions.profile()) }
  )

  // Painter unico para os tres estados: sem foto, carregando e falha. Sem ele o
  // AsyncImage sem tamanho colapsava para 0x0 e a area ficava visualmente vazia.
  val fallback = rememberVectorPainter(Icons.Outlined.Person)

  AsyncImage(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      // Fundo visivel: sem ele, sem foto, so o icone aparecia e o "toque no
      // circulo" do primeiro acesso apontava para um circulo invisivel.
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable { requestCamera.launch(Manifest.permission.CAMERA) },
    contentScale = ContentScale.Crop,
    model = ImageRequest.Builder(LocalContext.current)
      .data(photo)
      .crossfade(true)
      // Recorte pelo clip e nao por transformacao: transformacao so se aplica
      // ao bitmap carregado, entao o fallback ficaria quadrado.
      .build(),
    placeholder = fallback,
    error = fallback,
    fallback = fallback,
    contentDescription = stringResource(R.string.profile_image_description)
  )
}

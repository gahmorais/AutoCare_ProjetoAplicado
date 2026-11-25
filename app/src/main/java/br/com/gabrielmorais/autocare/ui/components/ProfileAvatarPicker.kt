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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.vector.RenderVectorGroup
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
  // concedida mesmo sendo o cropper quem abre a camera. Negada, a galeria
  // continua disponivel - antes o toque simplesmente nao fazia nada.
  val requestCamera = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted -> cropImage.launch(PhotoCropOptions.profile(includeCamera = granted)) }
  )

  ProfileAvatar(
    modifier = modifier,
    photo = photo,
    size = size,
    contentDescription = stringResource(R.string.profile_image_description),
    onClick = { requestCamera.launch(Manifest.permission.CAMERA) }
  )
}

/**
 * Avatar redondo, sem acao propria. [photo] e o que o Coil exibe: File, Uri ou
 * null para o icone de pessoa.
 */
@Composable
fun ProfileAvatar(
  photo: Any?,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  contentDescription: String? = null,
  onClick: (() -> Unit)? = null
) {
  // Painter unico para os tres estados: sem foto, carregando e falha. Sem ele o
  // AsyncImage sem tamanho colapsava para 0x0 e a area ficava visualmente vazia.
  //
  // O vetor sai preto por padrao e sumia sobre o surfaceVariant do tema escuro.
  // A cor vai so no painter do icone: um colorFilter no AsyncImage tingiria a
  // foto tambem.
  val icon = Icons.Outlined.Person
  val fallback = rememberVectorPainter(
    defaultWidth = icon.defaultWidth,
    defaultHeight = icon.defaultHeight,
    viewportWidth = icon.viewportWidth,
    viewportHeight = icon.viewportHeight,
    name = icon.name,
    tintColor = MaterialTheme.colorScheme.onSurfaceVariant,
    tintBlendMode = BlendMode.SrcIn,
    autoMirror = icon.autoMirror,
    content = { _, _ -> RenderVectorGroup(group = icon.root) }
  )

  AsyncImage(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      // Fundo visivel: sem ele, sem foto, so o icone aparecia e o "toque no
      // circulo" do primeiro acesso apontava para um circulo invisivel.
      .background(MaterialTheme.colorScheme.surfaceVariant)
      // Depois do clip, para o efeito do toque tambem ficar redondo.
      .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
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
    contentDescription = contentDescription
  )
}

package br.com.gabrielmorais.autocare.ui.activities.account_screen

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.images.CloudinaryTransformations
import br.com.gabrielmorais.autocare.data.images.CloudinaryUrl
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView

private val AVATAR_SIZE = 112.dp

/**
 * Perfil e sessao. O que era "Minha Conta" menos a lista de veiculos, que foi
 * para a aba Veiculos, e mais o sair, que vivia num menu de tres pontinhos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
  viewModel: MainViewModel,
  onLoggedOut: () -> Unit
) {
  val user by viewModel.user.collectAsState(initial = null)
  var email by remember(user?.email) { mutableStateOf(user?.email.orEmpty()) }
  var name by remember(user?.name) { mutableStateOf(user?.name.orEmpty()) }

  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result -> result.uriContent?.let(viewModel::updateUserPhoto) }
  )
  val requestCamera = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
      if (granted) {
        takePicture.launch(
          CropImageContractOptions(
            null,
            CropImageOptions(
              imageSourceIncludeGallery = true,
              imageSourceIncludeCamera = true,
              guidelines = CropImageView.Guidelines.ON,
              aspectRatioX = 1,
              aspectRatioY = 1
            )
          )
        )
      }
    }
  )

  // Painter unico para os tres estados: sem foto, carregando e falha. Sem ele o
  // AsyncImage sem tamanho colapsava para 0x0 e a area ficava visualmente vazia.
  val avatarFallback = rememberVectorPainter(Icons.Outlined.Person)

  Scaffold(
    topBar = { TopAppBar(title = { Text(stringResource(R.string.text_my_account)) }) }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      AsyncImage(
        modifier = Modifier
          .padding(top = 16.dp)
          // O tamanho explicito garante que a area exista mesmo sem imagem.
          .size(AVATAR_SIZE)
          .clip(CircleShape)
          .clickable { requestCamera.launch(Manifest.permission.CAMERA) },
        contentScale = ContentScale.Crop,
        model = ImageRequest.Builder(LocalContext.current)
          .data(CloudinaryUrl.withTransformation(user?.photo, CloudinaryTransformations.AVATAR))
          .crossfade(true)
          // Recorte pelo clip e nao por transformacao: transformacao so se
          // aplica ao bitmap carregado, entao o fallback ficaria quadrado.
          .build(),
        placeholder = avatarFallback,
        error = avatarFallback,
        fallback = avatarFallback,
        contentDescription = stringResource(R.string.profile_image_description)
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.text_email)) },
        enabled = false,
        value = email,
        onValueChange = { email = it }
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.text_name)) },
        placeholder = { Text(stringResource(R.string.name_placeholder)) },
        value = name,
        onValueChange = { name = it }
      )

      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          user?.let { viewModel.updateUser(it.copy(name = name, email = email, vehicles = null)) }
        }
      ) {
        Text(stringResource(R.string.text_update_user_data))
      }

      TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { viewModel.changePassword(user?.email.orEmpty()) }
      ) {
        Text(stringResource(R.string.text_change_password))
      }

      OutlinedButton(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 24.dp),
        onClick = {
          viewModel.logout()
          onLoggedOut()
        }
      ) {
        Text(stringResource(R.string.text_exit))
      }
    }
  }
}

package br.com.gabrielmorais.autocare.ui.activities.account_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.components.ProfileAvatarPicker
import br.com.gabrielmorais.autocare.ui.components.rememberLocalPhoto

/**
 * Perfil local: nome e foto de quem usa o app. Nao ha conta, senha nem sair -
 * os dados ficam no aparelho. A troca entre perfis chega na etapa 6 do plano.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
  val profile by viewModel.activeProfile.collectAsState()
  var name by remember(profile?.id, profile?.name) { mutableStateOf(profile?.name.orEmpty()) }

  Scaffold(
    topBar = { TopAppBar(title = { Text(stringResource(R.string.text_profile)) }) }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      ProfileAvatarPicker(
        modifier = Modifier.padding(top = 16.dp),
        photo = rememberLocalPhoto(profile?.photoPath),
        onPhotoPicked = viewModel::updateProfilePhoto
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.text_name)) },
        placeholder = { Text(stringResource(R.string.name_placeholder)) },
        singleLine = true,
        value = name,
        onValueChange = { name = it }
      )

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = name.isNotBlank() && name.trim() != profile?.name,
        onClick = { viewModel.updateProfileName(name) }
      ) {
        Text(stringResource(R.string.text_update_user_data))
      }
    }
  }
}

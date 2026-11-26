package br.com.gabrielmorais.autocare.ui.activities.profile_screen

import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.utils.findActivity
import br.com.gabrielmorais.autocare.data.ads.LocalAdsController
import androidx.compose.material.icons.outlined.PrivacyTip
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.components.ProfileAvatar
import br.com.gabrielmorais.autocare.ui.components.ProfileAvatarPicker
import br.com.gabrielmorais.autocare.ui.components.rememberLocalPhoto

/**
 * Perfil ativo (nome e foto) e os demais perfis do aparelho. Nao ha conta,
 * senha nem sair - os dados ficam no aparelho, separados por pessoa.
 *
 * @param backupSection a secao de exportar e importar ([BackupSection]). E um
 * slot para a tela continuar testavel so com a [MainViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, backupSection: @Composable () -> Unit = {}) {
  val profile by viewModel.activeProfile.collectAsState()
  val profiles by viewModel.profiles.collectAsState()
  val vehicles by viewModel.allVehicles.collectAsState()
  var name by remember(profile?.id, profile?.name) { mutableStateOf(profile?.name.orEmpty()) }
  var showAddDialog by rememberSaveable { mutableStateOf(false) }
  var toDelete by remember { mutableStateOf<Profile?>(null) }
  val context = LocalContext.current

  Scaffold(
    // safeDrawing inclui o teclado: com edge-to-edge a janela nao encolhe
    // sozinha, e o campo em foco ficaria escondido atras dele.
    contentWindowInsets = WindowInsets.safeDrawing,
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

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .semantics { heading() },
        text = stringResource(R.string.profiles_title),
        style = MaterialTheme.typography.titleMedium
      )
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.profiles_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      profiles.forEach { item ->
        ProfileRow(
          profile = item,
          vehicleCount = vehicles.count { it.profileId == item.id },
          isActive = item.id == profile?.id,
          canDelete = profiles.size > 1,
          onSelect = { viewModel.selectProfile(item.id) },
          onDelete = { toDelete = item }
        )
      }

      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showAddDialog = true }
      ) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.profiles_add))
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
      backupSection()
      PrivacyOptionsItem()
      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  if (showAddDialog) {
    AddProfileDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { newName ->
        showAddDialog = false
        viewModel.createProfile(newName)
      }
    )
  }

  toDelete?.let { target ->
    val owned = vehicles.filter { it.profileId == target.id }
    val deletedMessage = stringResource(R.string.profiles_deleted, target.name)
    DeleteProfileDialog(
      profile = target,
      vehicles = owned,
      onDismiss = { toDelete = null },
      onConfirm = {
        toDelete = null
        viewModel.deleteProfile(target.id) { removed ->
          // As manutencoes sairam em cascata; os alarmes delas tambem precisam
          // sair, senao o lembrete chegaria para um carro que nao existe.
          removed.flatMap { it.maintenances.orEmpty() }
            .forEach { NotificationUtils.cancelNotification(context, it) }
          Toast.makeText(context, deletedMessage, Toast.LENGTH_SHORT).show()
        }
      }
    )
  }
}

@Composable
private fun ProfileRow(
  profile: Profile,
  vehicleCount: Int,
  isActive: Boolean,
  canDelete: Boolean,
  onSelect: () -> Unit,
  onDelete: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(enabled = !isActive, onClickLabel = stringResource(R.string.profiles_use), onClick = onSelect)
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    ProfileAvatar(photo = rememberLocalPhoto(profile.photoPath), size = 40.dp)
    Column(modifier = Modifier.weight(1f)) {
      Text(text = profile.name, style = MaterialTheme.typography.bodyLarge)
      Text(
        text = pluralStringResource(R.plurals.profiles_vehicle_count, vehicleCount, vehicleCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    if (isActive) {
      Text(
        text = stringResource(R.string.profiles_active_label),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
      )
    }
    if (canDelete) {
      IconButton(onClick = onDelete) {
        Icon(
          imageVector = Icons.Outlined.Delete,
          contentDescription = stringResource(R.string.profiles_delete, profile.name)
        )
      }
    }
  }
}

@Composable
private fun AddProfileDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
  var newName by rememberSaveable { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.profiles_add)) },
    text = {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.text_name)) },
        placeholder = { Text(stringResource(R.string.name_placeholder)) },
        singleLine = true,
        value = newName,
        onValueChange = { newName = it },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
      )
    },
    confirmButton = {
      TextButton(enabled = newName.isNotBlank(), onClick = { onConfirm(newName.trim()) }) {
        Text(stringResource(R.string.profiles_add_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_cancel)) }
    }
  )
}

/** Diz o que vai junto antes de apagar: sem desfazer, a pessoa precisa saber o tamanho da perda. */
@Composable
private fun DeleteProfileDialog(
  profile: Profile,
  vehicles: List<Vehicle>,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  val maintenanceCount = vehicles.sumOf { it.maintenances.orEmpty().size }
  val body = if (vehicles.isEmpty()) {
    stringResource(R.string.profiles_delete_body_empty)
  } else {
    stringResource(
      R.string.profiles_delete_body,
      pluralStringResource(R.plurals.profiles_vehicle_count, vehicles.size, vehicles.size),
      pluralStringResource(R.plurals.profiles_maintenance_count, maintenanceCount, maintenanceCount)
    )
  }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.profiles_delete_title, profile.name)) },
    text = { Text(body) },
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(stringResource(R.string.text_delete), color = MaterialTheme.colorScheme.error)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_cancel)) }
    }
  )
}

/**
 * Rever o consentimento de anuncios. O UMP exige este ponto de entrada quando
 * o consentimento se aplica (por exemplo, no EEE e no Reino Unido); fora disso
 * o item nao aparece.
 */
@Composable
private fun PrivacyOptionsItem() {
  val ads = LocalAdsController.current
  val required by ads.privacyOptionsRequired.collectAsState()
  val context = LocalContext.current
  if (required) {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    OutlinedButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = { context.findActivity()?.let(ads::showPrivacyOptions) }
    ) {
      Icon(Icons.Outlined.PrivacyTip, contentDescription = null)
      Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.ads_privacy_options))
    }
  }
}

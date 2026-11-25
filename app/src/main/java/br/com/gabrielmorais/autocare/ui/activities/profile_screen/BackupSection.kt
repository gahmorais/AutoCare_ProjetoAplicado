package br.com.gabrielmorais.autocare.ui.activities.profile_screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.backup.BackupFormat
import br.com.gabrielmorais.autocare.data.backup.BackupPreview

/**
 * Exportar e importar dados em .zip, na tela de Perfil. O seletor do sistema
 * deixa a pessoa escolher onde guardar: Drive, Downloads, WhatsApp...
 */
@Composable
fun BackupSection(viewModel: BackupViewModel) {
  val state by viewModel.state.collectAsState()
  val exportLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument(BackupFormat.MIME_TYPE)
  ) { uri -> uri?.let(viewModel::export) }
  val importLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.OpenDocument()
  ) { uri -> uri?.let(viewModel::readImport) }

  BackupSectionContent(
    state = state,
    onExport = { exportLauncher.launch(viewModel.suggestedFileName()) },
    // Alguns provedores informam o .zip como octet-stream ou x-zip-compressed.
    onImport = { importLauncher.launch(IMPORT_MIME_TYPES) },
    onConfirmImport = viewModel::confirmImport,
    onCancelImport = viewModel::cancelImport,
    onDismissResult = viewModel::dismissResult
  )
}

private val IMPORT_MIME_TYPES = arrayOf(
  BackupFormat.MIME_TYPE,
  "application/x-zip-compressed",
  "application/octet-stream"
)

@Composable
fun BackupSectionContent(
  state: BackupUiState,
  onExport: () -> Unit,
  onImport: () -> Unit,
  onConfirmImport: () -> Unit,
  onCancelImport: () -> Unit,
  onDismissResult: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
      text = stringResource(R.string.backup_title),
      style = MaterialTheme.typography.titleMedium
    )
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(R.string.backup_hint),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !state.busy, onClick = onExport) {
      Icon(Icons.Outlined.FileUpload, contentDescription = null)
      Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.backup_export))
    }
    OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !state.busy, onClick = onImport) {
      Icon(Icons.Outlined.FileDownload, contentDescription = null)
      Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.backup_import))
    }

    state.operation?.let { operation ->
      LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      Text(
        text = stringResource(
          when (operation) {
            BackupOperation.EXPORTING -> R.string.backup_exporting
            BackupOperation.READING -> R.string.backup_reading
            BackupOperation.IMPORTING -> R.string.backup_importing
          }
        ),
        style = MaterialTheme.typography.bodySmall
      )
    }
  }

  state.preview?.let { preview ->
    AlertDialog(
      onDismissRequest = onCancelImport,
      title = { Text(stringResource(R.string.backup_preview_title)) },
      text = { Text(previewText(preview) + "\n\n" + stringResource(R.string.backup_preview_merge_note)) },
      confirmButton = {
        TextButton(onClick = onConfirmImport) { Text(stringResource(R.string.backup_import_confirm)) }
      },
      dismissButton = {
        TextButton(onClick = onCancelImport) { Text(stringResource(R.string.text_cancel)) }
      }
    )
  }

  state.result?.let { result ->
    AlertDialog(
      onDismissRequest = onDismissResult,
      title = {
        Text(
          stringResource(
            when (result) {
              is BackupResult.Exported -> R.string.backup_exported_title
              is BackupResult.Imported -> R.string.backup_imported_title
              BackupResult.NothingToImport -> R.string.backup_nothing_title
              is BackupResult.Failed -> R.string.backup_failed_title
            }
          )
        )
      },
      text = {
        Text(
          when (result) {
            is BackupResult.Exported -> stringResource(
              R.string.backup_exported_body,
              listOf(
                pluralStringResource(R.plurals.backup_profile_count, result.profiles, result.profiles),
                pluralStringResource(R.plurals.profiles_vehicle_count, result.vehicles, result.vehicles),
                pluralStringResource(
                  R.plurals.profiles_maintenance_count, result.maintenances, result.maintenances
                )
              ).joinToString(", ")
            )
            is BackupResult.Imported -> previewText(result.preview)
            BackupResult.NothingToImport -> stringResource(R.string.backup_nothing_body)
            is BackupResult.Failed -> result.message
          }
        )
      },
      confirmButton = {
        TextButton(onClick = onDismissResult) { Text(stringResource(R.string.text_ok)) }
      }
    )
  }
}

/** "Novos: 1 perfil, 2 veículos · Atualizados: 3 manutenções". So o que nao e zero. */
@Composable
private fun previewText(preview: BackupPreview): String {
  @Composable
  fun counts(profiles: Int, vehicles: Int, maintenances: Int): String = listOfNotNull(
    profiles.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.backup_profile_count, it, it) },
    vehicles.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.profiles_vehicle_count, it, it) },
    maintenances.takeIf { it > 0 }
      ?.let { pluralStringResource(R.plurals.profiles_maintenance_count, it, it) }
  ).joinToString(", ")

  return listOfNotNull(
    if (preview.hasNew) {
      stringResource(
        R.string.backup_preview_new,
        counts(preview.newProfiles, preview.newVehicles, preview.newMaintenances)
      )
    } else null,
    if (preview.hasUpdated) {
      stringResource(
        R.string.backup_preview_updated,
        counts(preview.updatedProfiles, preview.updatedVehicles, preview.updatedMaintenances)
      )
    } else null
  ).joinToString("\n")
}

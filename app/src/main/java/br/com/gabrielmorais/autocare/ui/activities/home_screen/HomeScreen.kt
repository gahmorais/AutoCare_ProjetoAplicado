package br.com.gabrielmorais.autocare.ui.activities.home_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import java.time.LocalDate

/**
 * O que precisa de atencao agora, somando todos os veiculos. A tela inicial
 * antiga listava carros com placa e km/mes e nao dizia nada sobre vencimentos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  onOpenVehicle: (String) -> Unit,
  onOpenMaintenance: (vehicleId: String, maintenanceId: Int) -> Unit,
  onAddVehicle: () -> Unit,
  today: LocalDate = LocalDate.now()
) {
  val user by viewModel.user.collectAsState(initial = null)
  val vehicles = user?.vehicles.orEmpty()
  val summary = remember(vehicles, today) { vehicles.homeSummary(today) }

  Scaffold(
    topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
  ) { padding ->
    when {
      vehicles.isEmpty() -> EmptyState(
        modifier = Modifier.padding(padding),
        title = stringResource(R.string.text_any_car_registered),
        body = stringResource(R.string.text_empty_vehicles_hint),
        actionLabel = stringResource(R.string.text_add_vehicle),
        onAction = onAddVehicle
      )

      // Ha carro mas nenhuma manutencao: nao ha o que estar "em dia", e dizer
      // isso mandaria o usuario a lugar nenhum.
      summary.totalCount == 0 -> EmptyState(
        modifier = Modifier.padding(padding),
        title = stringResource(R.string.does_not_have_maintenance),
        body = stringResource(R.string.text_empty_maintenances_hint)
      )

      summary.needsAttention.isEmpty() -> EmptyState(
        modifier = Modifier.padding(padding),
        title = stringResource(R.string.text_all_on_track),
        body = pluralStringResource(
          R.plurals.text_on_track_count,
          summary.onTrackCount,
          summary.onTrackCount
        )
      )

      else -> LazyColumn(
        modifier = Modifier.padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Text(
            modifier = Modifier.semantics { heading() },
            text = pluralStringResource(
              R.plurals.text_needs_attention,
              summary.needsAttention.size,
              summary.needsAttention.size
            ),
            style = MaterialTheme.typography.headlineSmall
          )
        }

        items(
          items = summary.needsAttention,
          key = { "${it.vehicleId}-${it.maintenance.id}" }
        ) { item ->
          SimpleCardMaintenance(
            modifier = Modifier.fillMaxWidth(),
            maintenance = item.maintenance,
            averageDistancePerMonth = item.averageDistancePerMonth,
            today = today,
            vehicleLabel = item.vehicleLabel,
            onClick = { onOpenMaintenance(item.vehicleId, item.maintenance.id) }
          )
        }

        if (summary.onTrackCount > 0) {
          item {
            Text(
              modifier = Modifier.padding(top = 8.dp),
              text = pluralStringResource(
                R.plurals.text_on_track_count,
                summary.onTrackCount,
                summary.onTrackCount
              ),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EmptyState(
  title: String,
  body: String,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .semantics { heading() },
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center
    )
    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      text = body,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
    if (actionLabel != null && onAction != null) {
      Button(
        modifier = Modifier.padding(top = 24.dp),
        onClick = onAction
      ) {
        Text(actionLabel)
      }
    }
  }
}

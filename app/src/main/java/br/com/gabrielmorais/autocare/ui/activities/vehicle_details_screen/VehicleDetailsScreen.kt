package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import br.com.gabrielmorais.autocare.ui.components.CardVehicleDetails
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

/**
 * Destino do NavHost. Antes era uma Activity que lia o id de um extra em
 * onStart e nao tinha botao de voltar.
 */
@Composable
fun VehicleDetailsRoute(
  vehicleId: String,
  onBack: () -> Unit,
  onAddMaintenance: (String) -> Unit,
  onEditMaintenance: (vehicleId: String, maintenanceId: Int) -> Unit,
  viewModel: VehicleDetailsViewModel = koinViewModel()
) {
  val context = LocalContext.current

  LaunchedEffect(vehicleId) {
    if (vehicleId.isNotBlank()) viewModel.getVehicle(vehicleId)
  }

  LaunchedEffect(Unit) {
    viewModel.message.collectLatest { message ->
      message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
  }

  VehicleDetailsScreen(
    viewModel = viewModel,
    onBack = onBack,
    onAddMaintenance = onAddMaintenance,
    onEditMaintenance = onEditMaintenance
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehicleDetailsScreen(
  viewModel: VehicleDetailsViewModel,
  onBack: () -> Unit,
  onAddMaintenance: (String) -> Unit,
  onEditMaintenance: (vehicleId: String, maintenanceId: Int) -> Unit
) {
  val vehicle = viewModel.vehicle.collectAsState()
  val context = LocalContext.current
  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      result.uriContent?.let { imageUri -> viewModel.uploadVehiclePhoto(imageUri) }
    }
  )

  val launcherRequestCameraPermisison = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { isGranted ->
      if (isGranted) {
        val options = CropImageContractOptions(
          null,
          CropImageOptions(
            imageSourceIncludeGallery = true,
            imageSourceIncludeCamera = true,
            guidelines = CropImageView.Guidelines.ON,
            aspectRatioX = 2,
            aspectRatioY = 1
          )
        )
        takePicture.launch(options)
      }
    }
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = vehicle.value?.nickName ?: stringResource(R.string.vehicle_details_text)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = stringResource(R.string.content_desc_back)
            )
          }
        }
      )
    },
    floatingActionButton = {
      // Era um icone na app bar, com alvo menor e concorrendo com o titulo.
      FloatingActionButton(onClick = { vehicle.value?.id?.let(onAddMaintenance) }) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = stringResource(R.string.content_desc_add_maintenance)
        )
      }
    }
  ) { contentPadding ->
    val allMaintenances = vehicle.value?.maintenances.orEmpty()
    var filter by rememberSaveable { mutableStateOf(MaintenanceFilter.TODAS) }
    val visibleMaintenances = remember(allMaintenances, filter) {
      allMaintenances.filteredBy(filter).sortedForDisplay()
    }

    Column(
      Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(16.dp)
    ) {
      CardVehicleDetails(
        vehicle = vehicle.value ?: vehicleSample,
        onClick = {
          launcherRequestCameraPermisison.launch(Manifest.permission.CAMERA)
        }
      )
      Divider(modifier = Modifier.padding(vertical = 16.dp))
      Text(
        modifier = Modifier
          .padding(bottom = 8.dp)
          .semantics { heading() },
        text = stringResource(R.string.maintenance_text),
        style = MaterialTheme.typography.titleLarge
      )

      // Um `when` explicito e nao um encadeamento de `?:`/`let`: com o filtro
      // ligado, um elvis esconderia tambem a barra de filtros quando o
      // resultado viesse vazio, e o usuario ficaria preso numa tela em branco
      // sem como voltar para "Todas".
      when {
        allMaintenances.isEmpty() -> EmptyMaintenanceMessage(
          text = stringResource(R.string.does_not_have_maintenance)
        )

        else -> {
          MaintenanceFilterRow(
            selected = filter,
            maintenances = allMaintenances,
            onSelect = { filter = it }
          )

          if (visibleMaintenances.isEmpty()) {
            EmptyMaintenanceMessage(
              text = stringResource(
                when (filter) {
                  MaintenanceFilter.PENDENTES -> R.string.text_no_pending_maintenance
                  MaintenanceFilter.CONCLUIDAS -> R.string.text_no_completed_maintenance
                  MaintenanceFilter.TODAS -> R.string.does_not_have_maintenance
                }
              )
            )
          } else {
            LazyColumn {
              items(visibleMaintenances, key = { it.id }) { maintenance ->
                // No M3 o limiar saiu do SwipeToDismiss e virou positionalThreshold
                // do proprio estado, e confirmStateChange virou confirmValueChange.
                val dismissState = rememberDismissState(
                  confirmValueChange = { value ->
                    if (value == DismissValue.DismissedToStart) {
                      viewModel.deleteMaintenance(maintenance) { removed ->
                        NotificationUtils.cancelNotification(context, removed)
                      }
                    }
                    true
                  },
                  positionalThreshold = { distance -> distance * 0.66f }
                )
                // Column envolvendo o item para que marcar como concluida
                // deslize o card ate o fim da lista em vez de saltar - e
                // tambem porque o cartao e o Spacer eram dois nos irmaos
                // soltos no mesmo item.
                Column(modifier = Modifier.animateItemPlacement()) {
                  SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {
                      // So desenha enquanto o swipe acontece: fora dele nao ha
                      // nada atras do cartao para vazar por transparencia.
                      if (dismissState.dismissDirection == DismissDirection.EndToStart) {
                      Box(
                        modifier = Modifier
                          .fillMaxSize()
                          .clip(MaterialTheme.shapes.medium)
                          .background(MaterialTheme.colorScheme.errorContainer)
                      ) {
                        Icon(
                          modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 20.dp)
                            .size(24.dp),
                          imageVector = Icons.Rounded.Delete,
                          tint = MaterialTheme.colorScheme.onErrorContainer,
                          contentDescription = stringResource(R.string.content_desc_delete_maintenance)
                        )
                      }
                      }
                    },
                    dismissContent = {
                      SimpleCardMaintenance(
                        modifier = Modifier.fillMaxWidth(),
                        maintenance = maintenance,
                        averageDistancePerMonth = vehicle.value?.averageDistanceTraveledPerMonth,
                        onClick = {
                          vehicle.value?.id?.let { onEditMaintenance(it, maintenance.id) }
                        }
                      )
                    }
                  )
                  Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceFilterRow(
  selected: MaintenanceFilter,
  maintenances: List<Maintenance>,
  onSelect: (MaintenanceFilter) -> Unit
) {
  val pendingCount = maintenances.count { !it.completed }
  val completedCount = maintenances.size - pendingCount

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(bottom = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    val labels = listOf(
      MaintenanceFilter.TODAS to stringResource(R.string.text_filter_all, maintenances.size),
      MaintenanceFilter.PENDENTES to stringResource(R.string.text_filter_pending, pendingCount),
      MaintenanceFilter.CONCLUIDAS to stringResource(R.string.text_filter_completed, completedCount)
    )
    labels.forEach { (value, label) ->
      // No M3 o rotulo virou parametro `label` em vez de conteudo trailing.
      FilterChip(
        selected = selected == value,
        onClick = { onSelect(value) },
        label = { Text(text = label) },
        // Mesmo motivo da aba ativa: o secondaryContainer padrao e o oliva de
        // "em dia", e filtro selecionado nao pode falar a lingua do status.
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      )
    }
  }
}

@Composable
private fun EmptyMaintenanceMessage(text: String) {
  Column(
    modifier = Modifier
      .padding(top = 5.dp)
      .fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = text,
      style = MaterialTheme.typography.titleLarge,
      textAlign = TextAlign.Center
    )
  }
}

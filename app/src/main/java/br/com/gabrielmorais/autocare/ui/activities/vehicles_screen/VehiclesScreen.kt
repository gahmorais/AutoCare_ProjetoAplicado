package br.com.gabrielmorais.autocare.ui.activities.vehicles_screen

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.activities.my_account_screen.AddVehicleDialog
import br.com.gabrielmorais.autocare.ui.activities.my_account_screen.AddVehicleDialogState
import br.com.gabrielmorais.autocare.ui.components.CardVehicle

/**
 * A garagem. Absorve a lista de veiculos que vivia duplicada entre a tela
 * inicial e "Minha Conta", e traz o botao de adicionar para onde o usuario esta
 * quando percebe que falta um carro - antes ele ficava atras do drawer.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VehiclesScreen(
  viewModel: MainViewModel,
  onOpenVehicle: (String) -> Unit
) {
  val user by viewModel.user.collectAsState(initial = null)
  val vehicles = user?.vehicles.orEmpty()

  val dialogState = remember { AddVehicleDialogState() }
  var showDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val invalidDistanceMessage = stringResource(R.string.text_invalid_distance)

  Scaffold(
    topBar = { TopAppBar(title = { Text(stringResource(R.string.text_vehicles)) }) },
    floatingActionButton = {
      FloatingActionButton(onClick = { showDialog = true }) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = stringResource(R.string.text_add_vehicle)
        )
      }
    }
  ) { padding ->
    if (vehicles.isEmpty()) {
      Column(
        modifier = Modifier
          .padding(padding)
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() },
          text = stringResource(R.string.text_any_car_registered),
          style = MaterialTheme.typography.headlineSmall,
          textAlign = TextAlign.Center
        )
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
          text = stringResource(R.string.text_empty_vehicles_hint),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(items = vehicles, key = { it.id ?: it.hashCode().toString() }) { vehicle ->
          VehicleRow(
            vehicle = vehicle,
            onOpen = { vehicle.id?.let(onOpenVehicle) },
            onDelete = { vehicle.id?.let(viewModel::deleteVehicle) }
          )
        }
      }
    }

    if (showDialog) {
      AddVehicleDialog(
        state = dialogState,
        onDismiss = { showDialog = false },
        onConfirm = {
          // toInt() estourava NumberFormatException com o campo vazio.
          val averageDistance = dialogState.averageDistanceTraveled.toIntOrNull()
          if (averageDistance == null || averageDistance <= 0) {
            Toast.makeText(context, invalidDistanceMessage, Toast.LENGTH_SHORT).show()
          } else {
            viewModel.saveVehicle(
              Vehicle(
                nickName = dialogState.nickName,
                brand = dialogState.brand,
                model = dialogState.model,
                plate = dialogState.plate,
                photo = dialogState.photo,
                averageDistanceTraveledPerMonth = averageDistance
              )
            )
            showDialog = false
          }
        }
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun androidx.compose.foundation.lazy.LazyItemScope.VehicleRow(
  vehicle: Vehicle,
  onOpen: () -> Unit,
  onDelete: () -> Unit
) {
  val dismissState = rememberDismissState(
    confirmValueChange = { value ->
      if (value == DismissValue.DismissedToStart) onDelete()
      true
    },
    positionalThreshold = { distance -> distance * 0.66f }
  )

  SwipeToDismiss(
    modifier = Modifier.animateItemPlacement(),
    state = dismissState,
    directions = setOf(DismissDirection.EndToStart),
    background = {
      // So desenha enquanto o swipe acontece: fora dele nao ha nada atras do
      // cartao para vazar por transparencia.
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
            contentDescription = stringResource(R.string.content_desc_delete_vehicle)
          )
        }
      }
    },
    dismissContent = {
      CardVehicle(
        modifier = Modifier.fillMaxWidth(),
        vehicle = vehicle,
        onCardClick = onOpen
      )
    }
  )
}

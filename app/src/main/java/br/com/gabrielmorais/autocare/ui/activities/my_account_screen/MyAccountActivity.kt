package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_VEHICLE_ID
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyAccountActivity : ComponentActivity() {
  // by inject() criava uma instancia solta, fora do ViewModelStore, entao o
  // estado se perdia ao girar a tela.
  private val viewModel: MyAccountViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.observeUser()
    setContent {
      AutoCareTheme {
        MyAccountScreen(viewModel)
      }
    }

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        if (message.isNotBlank())
          Toast.makeText(this@MyAccountActivity, message, Toast.LENGTH_SHORT).show()
      }
    }
  }


}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MyAccountScreen(viewModel: MyAccountViewModel? = null) {

  val user = viewModel?.user?.collectAsState(initial = null)
  var email by remember(user?.value?.email) { mutableStateOf(user?.value?.email) }
  var name by remember(user?.value?.name) { mutableStateOf(user?.value?.name) }
  val addVehicleDialogState = remember { AddVehicleDialogState() }
  var showDialogAddVehicle by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val invalidDistanceMessage = stringResource(R.string.text_invalid_distance)
  Scaffold(
    topBar = { TopAppBar(title = { Text(text = stringResource(R.string.text_my_account)) }) }
  ) { contentPadding ->
    Column(
      Modifier
        .padding(contentPadding)
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.text_email)) },
        enabled = false,
        value = email ?: "",
        onValueChange = { email = it }
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = stringResource(R.string.text_name)) },
        placeholder = { Text(text = stringResource(R.string.name_placeholder)) },
        value = name ?: "",
        onValueChange = { name = it }
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        TextButton(onClick = {
          viewModel?.changePassword(user?.value?.email ?: "")
        }) {
          Text(
            text = stringResource(R.string.text_change_password),
            style = MaterialTheme.typography.labelLarge,
          )
        }
        TextButton(onClick = {
          user?.value?.let {
            val updatedUser = it.copy(
              id = it.id,
              name = name,
              email = email,
              vehicles = null
            )

            viewModel.updateUser(updatedUser)
          }
        }) {
          Text(
            text = stringResource(R.string.text_update_user_data),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }

      Text(
        text = stringResource(R.string.text_vehicles),
        style = TextStyle(
          textDecoration = TextDecoration.Underline,
        ).merge(MaterialTheme.typography.titleMedium)
      )

      Spacer(modifier = Modifier.padding(vertical = 16.dp))
      LazyColumn(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        itemsIndexed(items = user?.value?.vehicles ?: listOf(), key = { _, item ->
          item.id ?: 0
        }) { _, vehicle ->
          // No M3 o limiar saiu do SwipeToDismiss e virou positionalThreshold
          // do proprio estado, e confirmStateChange virou confirmValueChange.
          val state = rememberDismissState(
            confirmValueChange = {
              if (it == DismissValue.DismissedToStart) {
                vehicle.id?.let { id -> viewModel?.deleteVehicle(id) }
              }
              true
            },
            positionalThreshold = { distance -> distance * 0.66f }
          )
          SwipeToDismiss(
            modifier = Modifier.animateItemPlacement(),
            state = state,
            directions = setOf(DismissDirection.EndToStart),
            background = {
              val color = when (state.dismissDirection) {
                DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                else -> Color.Transparent
              }
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .clip(MaterialTheme.shapes.medium)
                  .background(color = color)
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
            },
            dismissContent = {
              CardVehicle(
                modifier = Modifier.fillMaxWidth(),
                vehicle = vehicle,
                onCardClick = {
                  val intent = Intent(context, VehicleDetailsActivity::class.java)
                  intent.putExtra(INTENT_VEHICLE_ID, vehicle.id ?: "")
                  context.startActivity(intent)
                }
              )
            }
          )
        }
      }
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDialogAddVehicle = showDialogAddVehicle.not() },
      ) {
        Text(text = stringResource(R.string.text_add_vehicle), style = MaterialTheme.typography.titleLarge)
      }

      if (showDialogAddVehicle) {
        AddVehicleDialog(
            addVehicleDialogState,
            onDismiss = {
              showDialogAddVehicle = false
            },
            onConfirm = {
              // toInt() estourava NumberFormatException com o campo vazio.
              val averageDistance = addVehicleDialogState.averageDistanceTraveled.toIntOrNull()
              if (averageDistance == null || averageDistance <= 0) {
                Toast.makeText(context, invalidDistanceMessage, Toast.LENGTH_SHORT).show()
              } else {
                val newVehicle = Vehicle(
                  nickName = addVehicleDialogState.nickName,
                  brand = addVehicleDialogState.brand,
                  model = addVehicleDialogState.model,
                  plate = addVehicleDialogState.plate,
                  photo = addVehicleDialogState.photo,
                  averageDistanceTraveledPerMonth = averageDistance
                )
                viewModel?.saveVehicle(newVehicle)
                showDialogAddVehicle = false
              }
            })
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MyAccountScreenPreview() {
  MyAccountScreen()
}
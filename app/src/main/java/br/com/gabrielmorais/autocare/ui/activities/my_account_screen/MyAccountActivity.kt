package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.Button
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_USER_ID
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_VEHICLE_ID
import br.com.gabrielmorais.autocare.utils.findActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class MyAccountActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyAccountScreen()
    }
  }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MyAccountScreen(viewModel: MyAccountViewModel = koinViewModel()) {

  val user by viewModel.user.collectAsState()
  var nickname by remember(user?.nickname) { mutableStateOf(user?.nickname ?: "") }
  var name by remember(user?.name) { mutableStateOf(user?.name ?: "") }
  var addVehicleDialogState = remember { AddVehicleDialogState() }
  var showDialogAddVehicle by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val activity = context.findActivity()
  val intent = activity?.intent

  LaunchedEffect(key1 = intent?.getStringExtra(INTENT_USER_ID)) {
    val userId = intent?.getStringExtra(INTENT_USER_ID)
    Timber.tag("MainActivity").i("User Id: $userId")
    if (userId != null) {
      withContext(Dispatchers.IO) { viewModel.getUser(userId = userId) }
    }
  }

  val message by viewModel.message.collectAsState()
  LaunchedEffect(key1 = message) {
    if (message.isNotEmpty()) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }


  Timber.tag("MyAccountActivity").i("MyAccountScreen: $user")
  AutoCareTheme {
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
          value = nickname,
          onValueChange = { nickname = it }
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          label = { Text(text = stringResource(R.string.text_name)) },
          placeholder = { Text(text = stringResource(R.string.name_placeholder)) },
          value = name,
          onValueChange = { name = it }
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          TextButton(onClick = {
            viewModel.changePassword(user?.nickname ?: "")
          }) {
            Text(
              text = stringResource(R.string.text_change_password),
              style = Typography.subtitle1,
            )
          }
          TextButton(onClick = {
            user?.let {
              val updatedUser = it.copy(
                id = it.id,
                name = name,
                nickname = nickname,
              )

              scope.launch { viewModel.updateUser(updatedUser) }
            }
          }) {
            Text(
              text = stringResource(R.string.text_update_user_data),
              style = Typography.subtitle1,
            )
          }
        }

        Text(
          text = stringResource(R.string.text_vehicles),
          style = TextStyle(
            textDecoration = TextDecoration.Underline,
          ).merge(Typography.h6)
        )

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        val vehicles by viewModel.vehicleList.collectAsState()

        LazyColumn(
          modifier = Modifier.padding(vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          itemsIndexed(items = vehicles, key = { _, item ->
            item.id
          }) { _, vehicle ->
            val state = rememberDismissState(
              confirmStateChange = {
                if (it == DismissValue.DismissedToStart) {
                  scope.launch { viewModel.deleteVehicle(vehicle) }
                }
                true
              }
            )
            SwipeToDismiss(
              modifier = Modifier
                .padding(vertical = 1.dp)
                .animateItemPlacement(),
              state = state,
              background = {
                val color = when (state.dismissDirection) {
                  DismissDirection.EndToStart -> Color.Red
                  else -> Color.Transparent
                }
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(color = color),

                  ) {
                  Icon(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .padding(end = 10.dp)
                      .size(25.dp),
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                  )
                }
              },

              directions = setOf(DismissDirection.EndToStart),
              dismissThresholds = { directions ->
                FractionalThreshold(0.66F)
              }
            ) {
              CardVehicle(
                modifier = Modifier.fillMaxWidth(),
                vehicle = vehicle,
                onCardClick = {
                  val openVehicleDetails = Intent(context, VehicleDetailsActivity::class.java)
                  val vehicleId = vehicle.id
                  openVehicleDetails.putExtra(INTENT_VEHICLE_ID, vehicleId)
                  context.startActivity(intent)
                }
              )
            }
          }
        }
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = { showDialogAddVehicle = showDialogAddVehicle.not() },
        ) {
          Text(text = stringResource(R.string.text_add_vehicle), style = Typography.h5)
        }

        if (showDialogAddVehicle) {
          Box(modifier = Modifier.background(Color.White)) {
            AddVehicleDialog(
              addVehicleDialogState,
              onDismiss = {
                showDialogAddVehicle = false
              },
              onConfirm = {
                val userId = user?.id ?: throw Exception("Usuário não atribuido")
                val newVehicle = Vehicle(
                  nickName = addVehicleDialogState.nickName,
                  brand = addVehicleDialogState.brand,
                  model = addVehicleDialogState.model,
                  plate = addVehicleDialogState.plate,
                  userId = userId,
                  averageDistanceTraveledPerMonth = addVehicleDialogState.averageDistanceTraveled.toInt()
                )
                Timber.tag("MyAccountActivity").i("Carro: $newVehicle")
                Timber.tag("MyAccountScreen").d("MyAccountScreen: $userId")
                scope.launch { viewModel.saveVehicle(newVehicle) }
                showDialogAddVehicle = false
                addVehicleDialogState = AddVehicleDialogState()
              }
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MyAccountScreenPreview() {
  MyAccountScreen()
}
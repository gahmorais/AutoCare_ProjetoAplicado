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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_USER_ID
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_VEHICLE_ID
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MyAccountActivity : ComponentActivity() {
  private val viewModel: MyAccountViewModel by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val userId = intent.getStringExtra(INTENT_USER_ID)

    userId?.let {
      viewModel.getUser(it)
    }
    setContent {
      MyAccountScreen(viewModel)
    }

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        if (message.isNotBlank())
          Toast.makeText(this@MyAccountActivity, message, Toast.LENGTH_SHORT).show()
      }
    }
  }


}


@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MyAccountScreen(viewModel: MyAccountViewModel? = null) {

  val user = viewModel?.user?.collectAsState(initial = null)
  var email by remember(user?.value?.email) { mutableStateOf(user?.value?.email) }
  var name by remember(user?.value?.name) { mutableStateOf(user?.value?.name) }
  val addVehicleDialogState = remember { AddVehicleDialogState() }
  var showDialogAddVehicle by remember { mutableStateOf(false) }

  val context = LocalContext.current
  Log.i("MyAccountActivity", "MyAccountScreen: $user")
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
              style = Typography.subtitle1,
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
        LazyColumn(
          modifier = Modifier.padding(vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          itemsIndexed(items = user?.value?.vehicles ?: listOf(), key = { _, item ->
            item.id ?: 0
          }) { _, vehicle ->
            val state = rememberDismissState(
              confirmStateChange = {
                if (it == DismissValue.DismissedToStart) {
                  val userId = user?.value?.id ?: ""
                  val vehicleId = vehicle.id ?: ""
                  viewModel?.deleteVehicle(userId, vehicleId)
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
                  val intent = Intent(context, VehicleDetailsActivity::class.java)
                  val userId = user?.value?.id ?: ""
                  val vehicleId = vehicle.id ?: ""
                  intent.putExtra(INTENT_USER_ID, userId)
                  intent.putExtra(INTENT_VEHICLE_ID, vehicleId)
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
                val newVehicle = Vehicle(
                  nickName = addVehicleDialogState.nickName,
                  brand = addVehicleDialogState.brand,
                  model = addVehicleDialogState.model,
                  plate = addVehicleDialogState.plate,
                  photo = addVehicleDialogState.photo,
                  averageDistanceTraveledPerMonth = addVehicleDialogState.averageDistanceTraveled.toInt()
                )
                user?.value?.id?.let {
                  Log.d("MyAccountScreen", "MyAccountScreen: $it")
                  viewModel.saveVehicle(it, newVehicle)
                  showDialogAddVehicle = false
                }
              })
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
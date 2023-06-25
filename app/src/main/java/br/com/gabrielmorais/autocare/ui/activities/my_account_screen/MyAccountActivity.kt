package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyAccountActivity : ComponentActivity() {
  private val viewModel = MyAccountViewModel(
    UserRepositoryImpl(Firebase.database)
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val userId = intent.getStringExtra("user_id")
    Log.i("MyAccountActivity", "onCreate: $userId")
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
      topBar = { TopAppBar(title = { Text(text = "Minha Conta") }) }
    ) { contentPadding ->
      Column(
        Modifier
          .padding(contentPadding)
          .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          label = { Text(text = "Email") },
          enabled = false,
          value = email ?: "",
          onValueChange = { email = it }
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          label = { Text(text = "Nome") },
          placeholder = { Text(text = "John Doe") },
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
              text = "Trocar senha",
              style = Typography.subtitle1,
            )
          }
          TextButton(onClick = {
            user?.value?.let {
              val updatedUser = it.copy(
                name = name
              )
              viewModel.updateUser(updatedUser)
            }
          }) {
            Text(
              text = "Atualizar dados",
              style = Typography.subtitle1,
            )
          }
        }

        Text(
          text = "Veículos",
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
          Text(text = "Adicionar veículo", style = Typography.h5)
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
                  photo = addVehicleDialogState.photo
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
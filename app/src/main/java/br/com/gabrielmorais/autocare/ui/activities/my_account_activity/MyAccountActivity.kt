package br.com.gabrielmorais.autocare.ui.activities.my_account_activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.UserRepositoryImpl
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyAccountActivity : ComponentActivity() {
  private val viewModel = MyAccountViewModel(
    UserRepositoryImpl(FirebaseDatabase.getInstance())
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
        Toast.makeText(this@MyAccountActivity, message, Toast.LENGTH_SHORT).show()
      }
    }
  }


}


@Composable
fun MyAccountScreen(viewModel: MyAccountViewModel? = null) {

  val user = viewModel?.user?.collectAsState(initial = null)
  var email by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }
  val addVehicleDialogState = remember { AddVehicleDialogState() }
  var showDialog by remember { mutableStateOf(false) }
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
          value = email,
          onValueChange = { email = it }
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          label = { Text(text = "Nome") },
          value = name,
          onValueChange = { name = it }
        )

        Row(modifier = Modifier.fillMaxWidth()) {
          TextButton(onClick = { /*TODO*/ }) {
            Text(
              text = "Trocar senha",
              style = Typography.subtitle1,
            )
          }
          TextButton(onClick = { /*TODO*/ }) {
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
        user?.value?.vehicles?.map { vehicle ->
          CardVehicle(
            modifier = Modifier.fillMaxWidth(),
            vehicle = vehicle,
          )
          Spacer(modifier = Modifier.padding(vertical = 8.dp))
        }
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = { showDialog = showDialog.not() },
        ) {
          Text(text = "Adicionar veículo", style = Typography.h5)
        }

        if (showDialog) {
          Box(modifier = Modifier.background(Color.White)) {
            AddVehicleDialog(
              addVehicleDialogState,
              onDismiss = {
                showDialog = false
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
                  showDialog = false
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
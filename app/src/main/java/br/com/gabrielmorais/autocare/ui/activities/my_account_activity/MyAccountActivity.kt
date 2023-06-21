package br.com.gabrielmorais.autocare.ui.activities.my_account_activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.UserRepositoryImpl
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.components.vehicleSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import com.google.firebase.database.FirebaseDatabase

class MyAccountActivity : ComponentActivity() {
  private val userRepositoryImpl = UserRepositoryImpl(FirebaseDatabase.getInstance())
  private lateinit var user: User
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MyAccountScreen()
    }
  }

  override fun onResume() {
    super.onResume()
    userRepositoryImpl.getUser("XVj33lNM4NZszFecDlBWOXU6b9i2") { userResult ->
      userResult?.let {
        user = it
      }
    }
  }
}


@Composable
fun MyAccountScreen() {
  var email by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }

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

        TextButton(onClick = { /*TODO*/ }) {
          Text(
            text = "Trocar senha",
            style = Typography.subtitle1,
          )
        }

        Text(
          text = "Veículos",
          style = TextStyle(
            textDecoration = TextDecoration.Underline,
          ).merge(Typography.h6)
        )

        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        listOf(vehicleSample).map {
          CardVehicle(
            modifier = Modifier.fillMaxWidth(),
            vehicle = vehicleSample,
          )
          Spacer(modifier = Modifier.padding(vertical = 8.dp))
        }
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = { /*TODO*/ },
        ) {
          Text(text = "Adicionar veículo", style = Typography.h5)
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
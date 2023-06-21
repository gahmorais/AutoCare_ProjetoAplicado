package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme

class VehicleDetailsActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        VehicleDetailsScreen()
      }
    }
  }
}

@Composable
fun VehicleDetailsScreen() {
  val context = LocalContext.current
  Scaffold(
    topBar = { TopAppBar(title = { Text(text = "Detalhes do veículo") }) },
    floatingActionButton = {
      FloatingActionButton(onClick = {
        Toast.makeText(context, "Adicionar manutenção", Toast.LENGTH_SHORT).show()
      }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { contentPadding ->
    Column(Modifier.padding(contentPadding)) {
    }
  }
}

@Preview(showBackground = true)
@Composable
fun VehicleDetailsScreenPreview() {
  VehicleDetailsScreen()
}
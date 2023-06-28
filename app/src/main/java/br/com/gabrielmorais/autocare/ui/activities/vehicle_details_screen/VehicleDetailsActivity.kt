package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import coil.compose.AsyncImage

class VehicleDetailsActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        VehicleDetailsScreen(vehicleSample)
      }
    }
  }
}

@Composable
fun VehicleDetailsScreen(vehicle: Vehicle) {
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
      Card() {
        AsyncImage(
          modifier = Modifier
            .height(150.dp)
            .fillMaxWidth(),
          model = vehicle.photo,
          contentDescription = "",
          placeholder = painterResource(id = R.drawable.car_repair_placeholder)
        )
      }
      Text(text = vehicle.brand ?: "")
      Text(text = vehicle.model ?: "")
      Text(text = vehicle.plate ?: "")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun VehicleDetailsScreenPreview() {
  VehicleDetailsScreen(vehicleSample)
}
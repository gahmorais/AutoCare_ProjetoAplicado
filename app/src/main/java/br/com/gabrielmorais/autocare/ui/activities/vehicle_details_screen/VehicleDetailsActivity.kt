package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
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
    Column(
      Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(16.dp)
    ) {
      Card(shape = RoundedCornerShape(8.dp)) {
        Column {
          AsyncImage(
            modifier = Modifier
              .fillMaxHeight(0.3F)
              .fillMaxWidth()
              .clickable(
                onClick = {
                  Toast
                    .makeText(context, "Clicou", Toast.LENGTH_SHORT)
                    .show()
                }
              ),
            model = vehicle.photo,
            contentDescription = "",
            placeholder = painterResource(id = R.drawable.car_repair_placeholder)
          )
          Row(Modifier.padding(bottom = 16.dp)) {
            Text(text = vehicle.brand ?: "", style = Typography.h5)
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            Text(text = vehicle.model ?: "", style = Typography.h5)
          }
          Text(text = vehicle.plate ?: "", style = Typography.h5)
        }
      }
      Divider(modifier = Modifier.padding(vertical = 16.dp))
      Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = "Manutenções", style = Typography.h5
      )
      vehicle.maintenanceRecord?.let { maintenanceList ->
        LazyColumn {
          items(maintenanceList) { maintenance ->
            SimpleCardMaintenance(
              modifier = Modifier
                .fillMaxWidth(),
              maintenance = maintenance
            )
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
          }
        }
      } ?: Column(
        modifier = Modifier
          .padding(top = 5.dp)
          .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = "Nenhuma manutenção foi realizada",
          style = Typography.h5,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun VehicleDetailsScreenPreview() {
  VehicleDetailsScreen(vehicleSample)
}
package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.theme.Typography
import coil.compose.AsyncImage

@Composable
fun CardVehicleDetails(
  modifier: Modifier = Modifier,
  vehicle: Vehicle,
  onClick: () -> Unit = {}
) {
  Card(modifier = modifier, elevation = 0.dp) {
    Column {
      AsyncImage(
        modifier = Modifier
          .fillMaxHeight(0.3F)
          .fillMaxWidth()
          .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
        model = vehicle.photo ?: R.drawable.car_photo,
        contentDescription = "",
        error = painterResource(id = R.drawable.error)
      )
      Row(Modifier.padding(start = 16.dp)) {
        Text(text = vehicle.brand ?: "", style = Typography.h5)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = vehicle.model ?: "", style = Typography.h5)
      }
      Text(
        modifier = Modifier.padding(start = 16.dp),
        text = vehicle.plate ?: "",
        style = Typography.h5
      )
    }
  }
}

@Preview
@Composable
fun CardVehicleDetails() {
  CardVehicle(vehicle = vehicleSample)
}
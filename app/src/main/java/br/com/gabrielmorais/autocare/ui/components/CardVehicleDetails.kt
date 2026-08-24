package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.images.CloudinaryTransformations
import br.com.gabrielmorais.autocare.data.images.CloudinaryUrl
import br.com.gabrielmorais.autocare.data.models.Vehicle
import coil.compose.AsyncImage

@Composable
fun CardVehicleDetails(
  modifier: Modifier = Modifier,
  vehicle: Vehicle,
  onClick: () -> Unit = {}
) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.medium
  ) {
    Column {
      AsyncImage(
        modifier = Modifier
          .fillMaxHeight(0.3F)
          .fillMaxWidth()
          .clickable(onClick = onClick),
        model = CloudinaryUrl.withTransformation(
          vehicle.photo,
          CloudinaryTransformations.VEHICLE_BANNER
        ) ?: R.drawable.car_photo,
        contentDescription = "",
        error = painterResource(id = R.drawable.car_photo)
      )
      Row(Modifier.padding(bottom = 16.dp)) {
        Text(text = vehicle.brand ?: "", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = vehicle.model ?: "", style = MaterialTheme.typography.titleLarge)
      }
      Text(text = vehicle.plate ?: "", style = MaterialTheme.typography.titleLarge)
    }
  }
}
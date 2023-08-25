package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.ui.theme.Typography
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun CardVehicleDetails(
  modifier: Modifier = Modifier,
  vehicle: Vehicle,
  onClick: () -> Unit = {}
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(8.dp)
  ) {
    Column {
      AsyncImage(
        modifier = Modifier
          .fillMaxHeight(0.3F)
          .fillMaxWidth()
          .clickable(onClick = onClick),
        model = ImageRequest
          .Builder(LocalContext.current)
          .data(vehicle.photo)
          .error(R.drawable.car_photo)
          .crossfade(true)
          .build(),
        contentDescription = "",
        error = painterResource(id = R.drawable.error)
      )
      Row(Modifier.padding(bottom = 16.dp)) {
        Text(text = vehicle.brand ?: "", style = Typography.h5)
        Spacer(modifier = Modifier.padding(horizontal = 5.dp))
        Text(text = vehicle.model ?: "", style = Typography.h5)
      }
      Text(text = vehicle.plate ?: "", style = Typography.h5)
    }
  }
}
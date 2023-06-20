package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation


private const val URL_VEHICLE_SAMPLE =
  "https://quatrorodas.abril.com.br/wp-content/uploads/2022/10/stepway10_001-1-e1665169601442.jpg?quality=70&strip=info&w=1280&h=720&crop=1"

@Composable
fun CardVehicle(
  vehicle: Vehicle,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
) {
  Card(
    modifier = modifier,
    backgroundColor = Color.LightGray,
    elevation = 5.dp,
  ) {
    Row(
      modifier
        .padding(5.dp)
        .clickable { onClick() },
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        modifier = Modifier.height(100.dp),
        model = ImageRequest
          .Builder(LocalContext.current)
          .data(vehicle.photo)
          .transformations(CircleCropTransformation())
          .crossfade(true)
          .build(),
        placeholder = painterResource(id = R.drawable.car_repair_placeholder),
        contentDescription = null,
        contentScale = ContentScale.Fit
      )
      Column(
        Modifier
          .padding(horizontal = 10.dp),
      ) {
        Text(text = vehicle.brand, style = TextStyle(fontSize = 25.sp))
        Text(text = vehicle.model, style = TextStyle(fontSize = 25.sp))
        Text(text = vehicle.plate, style = TextStyle(fontSize = 25.sp))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun CardVehiclePreview() {
  CardVehicle(vehicleSample)
}


val vehicleSample = Vehicle(
  brand = "Renault",
  model = "Sandero",
  plate = "PFB3631",
  photo = URL_VEHICLE_SAMPLE
)
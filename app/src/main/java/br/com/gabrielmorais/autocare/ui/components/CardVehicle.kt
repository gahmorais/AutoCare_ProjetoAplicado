package br.com.gabrielmorais.autocare.ui.components

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import java.text.NumberFormat
import java.util.Locale


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardVehicle(
  vehicle: Vehicle,
  modifier: Modifier = Modifier,
  onCardClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
) {

  Log.i("CardVehicle", "CardVehicle: ${vehicle}")
  Card(
    modifier = modifier,
    elevation = 5.dp,
  ) {
    Row(
      modifier
        .padding(5.dp)
        .combinedClickable(
          onClick = { onCardClick() },
          onLongClick = { onLongClick() }
        ),
      verticalAlignment = Alignment.CenterVertically
    ) {
      AsyncImage(
        modifier = Modifier.height(100.dp),
        model = ImageRequest
          .Builder(LocalContext.current)
          .data(vehicle.photo ?: R.drawable.icon_car)
          .transformations(CircleCropTransformation())
          .crossfade(true)
          .build(),
        error = painterResource(id = R.drawable.error),
        contentDescription = null,
        contentScale = ContentScale.Fit
      )
      Column(
        Modifier
          .padding(horizontal = 10.dp),
      ) {
        Text(text = vehicle.nickName ?: "")
        Row {
          Text(
            text = vehicle.brand ?: "",
            style = TextStyle(fontSize = 20.sp),
          )
          Spacer(
            modifier = Modifier
              .padding(horizontal = 5.dp),
          )
          Text(
            text = vehicle.model ?: "",
            style = TextStyle(fontSize = 20.sp),
          )
        }
        Text(
          text = vehicle.plate ?: "",
          style = TextStyle(fontSize = 25.sp),
        )
        Divider(thickness = 2.dp)
        Text(text = "Distância por mês: ")
        Row {
          Text(
            text = NumberFormat
              .getNumberInstance(Locale("pt", "BR"))
              .format(vehicle.averageDistanceTraveledPerMonth),
            style = TextStyle(fontSize = 25.sp)
          )
          Text(text = " Km", style = TextStyle(fontSize = 25.sp))
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun CardVehiclePreview() {
  CardVehicle(vehicleSample)
}
package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.images.CloudinaryTransformations
import br.com.gabrielmorais.autocare.data.images.CloudinaryUrl
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

  Card(
    modifier = modifier,
    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
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
          .data(
            CloudinaryUrl.withTransformation(
              vehicle.photo,
              CloudinaryTransformations.VEHICLE_THUMBNAIL
            ) ?: R.drawable.icon_car
          )
          .transformations(CircleCropTransformation())
          .crossfade(true)
          .build(),
        // URLs antigas do Firebase Storage estao mortas: cair no icone de carro
        // comunica melhor que um icone de imagem quebrada.
        error = painterResource(id = R.drawable.icon_car),
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
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(
            modifier = Modifier
              .padding(horizontal = 5.dp),
          )
          Text(
            text = vehicle.model ?: "",
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Text(
          text = vehicle.plate ?: "",
          style = MaterialTheme.typography.titleLarge,
        )
        Divider(thickness = 2.dp)
        Text(text = "Distância por mês: ")
        Row {
          Text(
            text = NumberFormat
              .getNumberInstance(Locale("pt", "BR"))
              .format(vehicle.averageDistanceTraveledPerMonth),
            style = MaterialTheme.typography.titleLarge
          )
          Text(text = " Km", style = MaterialTheme.typography.titleLarge)
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
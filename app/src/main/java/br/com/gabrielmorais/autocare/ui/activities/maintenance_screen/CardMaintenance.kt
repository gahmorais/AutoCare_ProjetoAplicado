package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.sampleData.maintenanceListSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Composable
fun CardMaintenance(
  modifier: Modifier = Modifier,
  maintenance: Maintenance
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "Descrição:", style = Typography.h6)
          Text(text = maintenance.description ?: "")
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "Data:", style = Typography.h6)
          Text(text = formatDate(maintenance.date!!))
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "Km Atual:", style = Typography.h6)
          Text(text = maintenance.currentMileage.toString())
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "Próxima Man. em Km:", style = Typography.h6)
          Text(text = maintenance.forecastNextExchangeMileage.toString())
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "Próxima Man. em Meses:", style = Typography.h6)
          Text(
            text = diffBetweenMaintenance(
              maintenance.date!!,
              maintenance.forecastNextExchangeDate!!
            ).months.toString()
          )
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = "OBS:", style = Typography.h6)
          Text(text = maintenance.comments!!)
        }
      }
    }
  }
}

private fun diffBetweenMaintenance(currentDate: LocalDate, futureDate: LocalDate): Period {
  return Period.between(currentDate, futureDate)
}

private fun formatDate(date: LocalDate): String {
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
  return date.format(dateTimeFormatter)
}

@Preview(showBackground = true)
@Composable
fun CardMaintenancePreview() {
  AutoCareTheme() {
    maintenanceListSample.forEach {
      CardMaintenance(maintenance = it)
    }
  }
}
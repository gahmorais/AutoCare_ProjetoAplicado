package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.sampleData.maintenanceListSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Utils.Companion.diffBetweenMaintenance
import br.com.gabrielmorais.autocare.utils.Utils.Companion.formatDate

@Composable
fun CardMaintenance(
  modifier: Modifier = Modifier,
  maintenance: Maintenance,
  onClick: () -> Unit = {}
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = stringResource(R.string.text_description), style = Typography.h5)
          Text(text = maintenance.description ?: "")
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = stringResource(R.string.text_date), style = Typography.h5)
          Text(text = formatDate(maintenance.date!!))
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = stringResource(R.string.text_current_mileage), style = Typography.h5)
          Text(text = maintenance.currentMileage.toString())
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = stringResource(R.string.text_next_maintenance_mileage), style = Typography.h5)
          Text(text = maintenance.forecastNextExchangeMileage.toString())
        }
        Row(
          horizontalArrangement = Arrangement.spacedBy(5.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          Text(text = stringResource(R.string.text_next_maintenance_months), style = Typography.h5)
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
          Text(text = stringResource(R.string.text_comments), style = Typography.h5)
          Text(text = maintenance.comments!!)
        }
      }
    }
  }
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
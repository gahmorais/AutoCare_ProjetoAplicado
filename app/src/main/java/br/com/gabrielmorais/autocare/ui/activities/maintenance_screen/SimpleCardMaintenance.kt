package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import br.com.gabrielmorais.autocare.utils.Utils.Companion.formatDate

@Composable
fun SimpleCardMaintenance(
  modifier: Modifier = Modifier,
  maintenance: Maintenance
) {
  Card(
    shape = RoundedCornerShape(8.dp)
  ) {
    Column(
      modifier = modifier.padding(8.dp),
      horizontalAlignment = Alignment.End
    ) {
      Text(text = maintenance.description!!, style = Typography.h5)
      Text(text = formatDate(maintenance.date!!), style = Typography.h5)
    }
  }
}


@Preview(showBackground = true)
@Composable
fun SimpleCardMaintenancePreview() {
  AutoCareTheme {
    maintenanceListSample.forEach {
      SimpleCardMaintenance(maintenance = it)
    }
  }
}
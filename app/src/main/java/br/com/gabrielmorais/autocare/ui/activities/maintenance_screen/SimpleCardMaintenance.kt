package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.sampleData.maintenanceListSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Utils.Companion.formatDate

@Composable
fun SimpleCardMaintenance(
  modifier: Modifier = Modifier,
  maintenance: Maintenance,
  onClick: () -> Unit = {}
) {
  Card(
    modifier = modifier
      .clickable(onClick = onClick)
      // Manutencao concluida fica esmaecida, mas nao apenas esmaecida: o
      // selo abaixo mantem o estado legivel em alto contraste e para quem
      // enxerga mal a diferenca de opacidade.
      .alpha(if (maintenance.completed) 0.5f else 1f)
      .border(BorderStroke(width = 1.dp, color = Color.Gray), shape = RoundedCornerShape(8.dp)),
  ) {
    // Modifier e nao `modifier`: reaplicar o modificador do card aqui repetia o
    // clickable e a borda na coluna interna.
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.End
    ) {
      // Registros gravados antes de uma mudanca de schema podem vir incompletos:
      // exibe um placeholder em vez de derrubar a tela.
      Text(
        text = maintenance.description ?: "-",
        style = Typography.h5,
        textDecoration = if (maintenance.completed) TextDecoration.LineThrough else null
      )
      if (maintenance.completed) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            modifier = Modifier
              .size(16.dp)
              .padding(end = 4.dp),
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null
          )
          Text(text = stringResource(R.string.text_maintenance_completed))
        }
      }
      Divider()
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = "Feito em:")
        Text(text = "Próxima:")
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = maintenance.date?.let { formatDate(it) } ?: "-",
          style = Typography.h5
        )
        Text(
          text = maintenance.forecastNextExchangeDate?.let { formatDate(it) } ?: "-",
          style = Typography.h5
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = stringResource(R.string.mileage_placeholder))
        Text(text = stringResource(R.string.mileage_placeholder))
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(text = maintenance.currentMileage.toString(), style = Typography.h5)
        Text(text = maintenance.forecastNextExchangeMileage.toString(), style = Typography.h5)
      }
    }
  }
}


@Preview(showBackground = true)
@Composable
fun SimpleCardMaintenancePreview() {
  AutoCareTheme {
    maintenanceListSample.forEach {
      Column(Modifier.padding(5.dp)) {
        SimpleCardMaintenance(
          modifier = Modifier.fillMaxWidth(),
          maintenance = it
        )
      }
    }
  }
}

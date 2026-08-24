package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.MaintenanceProgress
import br.com.gabrielmorais.autocare.data.models.MaintenanceStatus
import br.com.gabrielmorais.autocare.data.models.progress
import br.com.gabrielmorais.autocare.sampleData.maintenanceListSample
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.NumeroFont
import br.com.gabrielmorais.autocare.utils.Utils.Companion.formatDate
import java.time.LocalDate

/**
 * [averageDistancePerMonth] vem do veiculo e e o que permite estimar a
 * quilometragem de hoje. Sem ele o card ainda funciona: perde o marcador de
 * "agora" e a estimativa, e o status passa a se apoiar so na data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleCardMaintenance(
  modifier: Modifier = Modifier,
  maintenance: Maintenance,
  averageDistancePerMonth: Int? = null,
  today: LocalDate = LocalDate.now(),
  /** De qual veiculo. So aparece na tela inicial, que mistura varios. */
  vehicleLabel: String? = null,
  onClick: () -> Unit = {}
) {
  val progress = remember(maintenance, averageDistancePerMonth, today) {
    maintenance.progress(averageDistancePerMonth, today)
  }
  val concluida = progress.status == MaintenanceStatus.CONCLUIDA
  val tone = progress.status.tone()

  Card(
    onClick = onClick,
    // TalkBack anuncia o card como uma unidade em vez de seis textos soltos.
    modifier = modifier.semantics(mergeDescendants = true) {},
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 14.dp)
        // O esmaecimento fica no conteudo e nao no Card: alpha no container
        // torna a superficie translucida, e na lista o fundo do swipe-to-delete
        // aparecia atraves do cartao concluido. Manutencao concluida fica
        // esmaecida, mas nao apenas esmaecida - o selo e o texto riscado mantem
        // o estado legivel em alto contraste e para quem enxerga mal a
        // diferenca de opacidade.
        .alpha(if (concluida) 0.6f else 1f),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          vehicleLabel?.let { label ->
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          // Registros gravados antes de uma mudanca de schema podem vir
          // incompletos: exibe um placeholder em vez de derrubar a tela.
          Text(
            text = maintenance.description ?: "—",
            style = MaterialTheme.typography.titleMedium,
            textDecoration = if (concluida) TextDecoration.LineThrough else null
          )
          maintenance.comments?.takeIf { it.isNotBlank() }?.let { comentario ->
            Text(
              text = comentario,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        StatusPill(status = progress.status, tone = tone)
      }

      MileageRuler(
        startMileage = progress.startMileage,
        forecastMileage = progress.forecastMileage,
        estimatedMileage = progress.estimatedMileage,
        fraction = progress.fraction,
        tone = tone
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = maintenance.date?.let { formatDate(it) } ?: "—",
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = NumeroFont),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = maintenance.forecastNextExchangeDate?.let { formatDate(it) } ?: "—",
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = NumeroFont),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      FooterNote(progress = progress, concluida = concluida, tone = tone)
    }
  }
}

/** O que a barra sozinha nao diz: onde o carro esta agora, ou quanto ja passou. */
@Composable
private fun FooterNote(progress: MaintenanceProgress, concluida: Boolean, tone: Color) {
  val overshoot = progress.overshootKm
  val estimated = progress.estimatedMileage

  when {
    concluida -> Unit

    overshoot != null -> Text(
      text = stringResource(R.string.text_mileage_overshoot, formatMileage(overshoot)),
      style = MaterialTheme.typography.labelMedium,
      color = tone
    )

    estimated != null -> Text(
      text = stringResource(R.string.text_estimated_mileage, formatMileage(estimated)),
      style = MaterialTheme.typography.labelMedium.copy(fontFamily = NumeroFont),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    else -> Unit
  }
}

@Composable
private fun StatusPill(status: MaintenanceStatus, tone: Color) {
  Text(
    modifier = Modifier
      .clip(CircleShape)
      .background(tone.copy(alpha = 0.16f))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    text = stringResource(status.labelRes()),
    style = MaterialTheme.typography.labelSmall,
    color = tone
  )
}

/**
 * Os tres estados caem em papeis do M3 sem precisar de extensao de tema:
 * oliva/secondary libera, ambar/tertiary avisa, ferrugem/error cobra.
 */
@Composable
private fun MaintenanceStatus.tone(): Color = when (this) {
  MaintenanceStatus.EM_DIA, MaintenanceStatus.CONCLUIDA -> MaterialTheme.colorScheme.secondary
  MaintenanceStatus.VENCE_EM_BREVE -> MaterialTheme.colorScheme.tertiary
  MaintenanceStatus.VENCIDA -> MaterialTheme.colorScheme.error
}

private fun MaintenanceStatus.labelRes(): Int = when (this) {
  MaintenanceStatus.EM_DIA -> R.string.text_status_on_track
  MaintenanceStatus.VENCE_EM_BREVE -> R.string.text_status_due_soon
  MaintenanceStatus.VENCIDA -> R.string.text_status_overdue
  MaintenanceStatus.CONCLUIDA -> R.string.text_maintenance_completed
}

@Preview(showBackground = true)
@Composable
private fun SimpleCardMaintenancePreview() {
  AutoCareTheme {
    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      maintenanceListSample.take(4).forEach {
        SimpleCardMaintenance(
          modifier = Modifier.fillMaxWidth(),
          maintenance = it,
          averageDistancePerMonth = 1000
        )
      }
    }
  }
}

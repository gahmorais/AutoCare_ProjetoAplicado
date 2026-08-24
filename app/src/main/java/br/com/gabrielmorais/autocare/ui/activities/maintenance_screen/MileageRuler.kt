package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.ui.theme.NumeroFont
import java.text.NumberFormat
import java.util.Locale

private val TRACK_HEIGHT = 6.dp
private val MARKER_SIZE = 14.dp

/**
 * A regua de quilometragem: onde o servico foi feito, onde o carro esta agora e
 * onde a proxima troca vence, numa leitura so. Substitui a grade de oito textos
 * de rotulo e valor que o card usava.
 *
 * O marcador de "agora" fica na ponta da faixa preenchida - e por isso que ele
 * cai exatamente em [fraction] sem precisar medir a largura do trilho.
 */
@Composable
fun MileageRuler(
  startMileage: Int?,
  forecastMileage: Int?,
  estimatedMileage: Int?,
  fraction: Float,
  tone: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(MARKER_SIZE),
      contentAlignment = Alignment.CenterStart
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(TRACK_HEIGHT)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant)
      )
      Box(
        modifier = Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)),
        contentAlignment = Alignment.CenterEnd
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(TRACK_HEIGHT)
            .clip(CircleShape)
            .background(tone)
        )
        // Sem media mensal cadastrada nao ha estimativa, e a regua fica so
        // inicio-fim em vez de fingir uma posicao.
        if (estimatedMileage != null) {
          Box(
            modifier = Modifier
              .size(MARKER_SIZE)
              .clip(CircleShape)
              .background(tone)
              .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
          )
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
    ) {
      RulerLabel(
        text = formatMileageOrDash(startMileage),
        align = TextAlign.Start,
        modifier = Modifier.weight(1f)
      )
      RulerLabel(
        text = formatMileageOrDash(forecastMileage),
        align = TextAlign.End,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun RulerLabel(text: String, align: TextAlign, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier,
    text = text,
    textAlign = align,
    maxLines = 1,
    style = MaterialTheme.typography.labelMedium.copy(fontFamily = NumeroFont),
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )
}

private val MILEAGE_FORMAT: NumberFormat = NumberFormat.getIntegerInstance(Locale("pt", "BR"))

fun formatMileage(value: Int): String = MILEAGE_FORMAT.format(value)

private fun formatMileageOrDash(value: Int?): String = value?.let(::formatMileage) ?: "—"

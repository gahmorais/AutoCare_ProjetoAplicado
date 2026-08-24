package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Escala do M3. A anterior era `small=4, medium=4, large=0` e praticamente nao
 * era referenciada - cada componente hardcodava 8, 15 ou 20dp por conta propria.
 */
val Shapes = Shapes(
  extraSmall = RoundedCornerShape(4.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

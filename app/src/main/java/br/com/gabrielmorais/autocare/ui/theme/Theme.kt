package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

internal val LightColors = lightColorScheme(
  primary = PetroleoClaro,
  onPrimary = SuperficieClara,
  primaryContainer = PetroleoContainerClaro,
  onPrimaryContainer = PetroleoOnContainerClaro,

  secondary = OlivaClaro,
  onSecondary = SuperficieClara,
  secondaryContainer = OlivaContainerClaro,
  onSecondaryContainer = OlivaOnContainerClaro,

  tertiary = AmbarClaro,
  onTertiary = AmbarOnContainerClaro,
  tertiaryContainer = AmbarContainerClaro,
  onTertiaryContainer = AmbarOnContainerClaro,

  error = FerrugemClaro,
  onError = SuperficieClara,
  errorContainer = FerrugemContainerClaro,
  onErrorContainer = FerrugemOnContainerClaro,

  background = ConcretoClaro,
  onBackground = TintaClara,
  surface = SuperficieClara,
  onSurface = TintaClara,
  surfaceVariant = SuperficieVarianteClara,
  onSurfaceVariant = TintaSuaveClara,
  outline = ContornoClaro,
  outlineVariant = ContornoSuaveClaro,

  inverseSurface = SuperficieInvertidaClara,
  inverseOnSurface = TintaInvertidaClara,
  inversePrimary = PetroleoInvertidoClaro
)

internal val DarkColors = darkColorScheme(
  primary = PetroleoEscuro,
  onPrimary = PetroleoOnEscuro,
  primaryContainer = PetroleoContainerEscuro,
  onPrimaryContainer = PetroleoContainerClaro,

  secondary = OlivaEscuro,
  onSecondary = OlivaOnEscuro,
  secondaryContainer = OlivaContainerEscuro,
  onSecondaryContainer = OlivaContainerClaro,

  tertiary = AmbarEscuro,
  onTertiary = AmbarOnEscuro,
  tertiaryContainer = AmbarContainerEscuro,
  onTertiaryContainer = AmbarContainerClaro,

  error = FerrugemEscuro,
  onError = FerrugemOnEscuro,
  errorContainer = FerrugemContainerEscuro,
  onErrorContainer = FerrugemContainerClaro,

  background = ConcretoEscuro,
  onBackground = TintaEscura,
  surface = SuperficieEscura,
  onSurface = TintaEscura,
  surfaceVariant = SuperficieVarianteEscura,
  onSurfaceVariant = TintaSuaveEscura,
  outline = ContornoEscuro,
  outlineVariant = ContornoSuaveEscuro,

  inverseSurface = SuperficieInvertidaEscura,
  inverseOnSurface = TintaInvertidaEscura,
  inversePrimary = PetroleoInvertidoEscuro
)

/**
 * Sem dynamic color de proposito: o sistema de estado da manutencao depende de
 * ambar e ferrugem significarem sempre a mesma coisa, e o papel-de-parede do
 * usuario reescreveria essas cores.
 */
@Composable
fun AutoCareTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalReducedMotion provides rememberReducedMotion()) {
    MaterialTheme(
      colorScheme = if (darkTheme) DarkColors else LightColors,
      typography = Typography,
      shapes = Shapes,
      content = content
    )
  }
}

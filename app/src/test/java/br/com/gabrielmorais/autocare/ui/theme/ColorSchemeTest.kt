package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O ColorScheme do M3 tem papeis que e facil esquecer de definir, e o que fica
 * de fora cai no baseline roxo do Material - foi assim que o rotulo "Desfazer"
 * do snackbar apareceu roxo depois de a migracao ter tirado o roxo de todo o
 * resto do app. Este teste varre o esquema inteiro em vez de conferir cor por
 * cor a cada papel novo que passar a ser usado.
 */
class ColorSchemeTest {

  /** Tons do baseline do Material 3 que nao devem sobrar em lugar nenhum. */
  private val baselineRoxo = setOf(
    Color(0xFF6750A4), // primary claro
    Color(0xFFD0BCFF), // primary escuro / inversePrimary claro
    Color(0xFFEADDFF), // primaryContainer claro
    Color(0xFF4F378B), // primaryContainer escuro
    Color(0xFF625B71), // secondary claro
    Color(0xFFCCC2DC), // secondary escuro
    Color(0xFF7D5260), // tertiary claro
    Color(0xFFEFB8C8)  // tertiary escuro
  )

  private fun ColorScheme.papeis(): Map<String, Color> = mapOf(
    "primary" to primary,
    "onPrimary" to onPrimary,
    "primaryContainer" to primaryContainer,
    "onPrimaryContainer" to onPrimaryContainer,
    "secondary" to secondary,
    "onSecondary" to onSecondary,
    "secondaryContainer" to secondaryContainer,
    "onSecondaryContainer" to onSecondaryContainer,
    "tertiary" to tertiary,
    "onTertiary" to onTertiary,
    "tertiaryContainer" to tertiaryContainer,
    "onTertiaryContainer" to onTertiaryContainer,
    "error" to error,
    "onError" to onError,
    "errorContainer" to errorContainer,
    "onErrorContainer" to onErrorContainer,
    "background" to background,
    "onBackground" to onBackground,
    "surface" to surface,
    "onSurface" to onSurface,
    "surfaceVariant" to surfaceVariant,
    "onSurfaceVariant" to onSurfaceVariant,
    "outline" to outline,
    "outlineVariant" to outlineVariant,
    "inverseSurface" to inverseSurface,
    "inverseOnSurface" to inverseOnSurface,
    "inversePrimary" to inversePrimary,
    "surfaceTint" to surfaceTint
  )

  private fun semBaseline(nome: String, scheme: ColorScheme) {
    val sobras = scheme.papeis().filterValues { it in baselineRoxo }
    assertTrue(
      "$nome ainda usa o baseline do Material nos papeis: ${sobras.keys}",
      sobras.isEmpty()
    )
  }

  @Test
  fun `tema claro nao carrega nenhuma cor do baseline`() = semBaseline("Tema claro", LightColors)

  @Test
  fun `tema escuro nao carrega nenhuma cor do baseline`() = semBaseline("Tema escuro", DarkColors)

  @Test
  fun `a superficie invertida do snackbar contrasta com o texto`() {
    // O snackbar desenha inverseOnSurface sobre inverseSurface; iguais, o texto
    // some. Comparar a luminancia e o suficiente para pegar o caso degenerado.
    listOf("claro" to LightColors, "escuro" to DarkColors).forEach { (nome, scheme) ->
      val fundo = scheme.inverseSurface.luminance()
      val texto = scheme.inverseOnSurface.luminance()
      assertTrue(
        "No tema $nome o texto do snackbar nao contrasta com o fundo",
        kotlin.math.abs(fundo - texto) > 0.4f
      )
    }
  }

  /** Luminancia aproximada, suficiente para distinguir claro de escuro. */
  private fun Color.luminance(): Float = 0.299f * red + 0.587f * green + 0.114f * blue
}

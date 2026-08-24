package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R

private val MontserratFont = FontFamily(
  Font(R.font.montserrat_thin, FontWeight.W100),
  Font(R.font.montserrat_extralight, FontWeight.W200),
  Font(R.font.montserrat_light, FontWeight.W300),
  Font(R.font.montserrat_regular, FontWeight.W400),
  Font(R.font.montserrat_medium, FontWeight.W500),
  Font(R.font.montserrat_semibold, FontWeight.W600),
  Font(R.font.montserrat_bold, FontWeight.W700)
)

/**
 * Quilometragem e data sao o dado central deste app e so alinham em coluna com
 * numerais de largura fixa. A monoespacada do sistema (Roboto Mono no Android)
 * cobre isso sem somar peso ao APK.
 */
val NumeroFont = FontFamily.Monospace

/**
 * Escala completa. A versao anterior definia quatro estilos e deixava o resto no
 * default, entao `h4` e `subtitle1`, usados nas telas, caiam no Roboto e o app
 * misturava duas familias sem querer.
 */
val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W700,
    fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1).sp
  ),
  displayMedium = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W700,
    fontSize = 42.sp, lineHeight = 48.sp, letterSpacing = (-0.8).sp
  ),
  displaySmall = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W700,
    fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.5).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W700,
    fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp
  ),
  headlineMedium = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W700,
    fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.3).sp
  ),
  headlineSmall = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.2).sp
  ),
  titleLarge = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.1).sp
  ),
  titleMedium = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp
  ),
  titleSmall = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W400,
    fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W400,
    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp
  ),
  bodySmall = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W400,
    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp
  ),
  labelLarge = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
  ),
  labelSmall = TextStyle(
    fontFamily = MontserratFont, fontWeight = FontWeight.W600,
    fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp
  )
)

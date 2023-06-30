package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R

// Set of Material typography styles to start with
private val thin = Font(R.font.montserrat_thin, FontWeight.W100)
private val extralight = Font(R.font.montserrat_extralight, FontWeight.W200)
private val light = Font(R.font.montserrat_light, FontWeight.W300)
private val regular = Font(R.font.montserrat_regular, FontWeight.W400)
private val medium = Font(R.font.montserrat_medium, FontWeight.W500)
private val semibold = Font(R.font.montserrat_semibold, FontWeight.W600)
private val bold = Font(R.font.montserrat_bold, FontWeight.W700)

private val MonteserratFont = FontFamily(
  listOf(
    thin,
    extralight,
    light,
    regular,
    medium,
    semibold,
    bold
  )
)
val Typography = Typography(
  body1 = TextStyle(
    fontFamily = MonteserratFont,
    fontWeight = FontWeight.W500,
    fontSize = 16.sp
  ),
  h6 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
  ),
  h5 = TextStyle(
    fontFamily = MonteserratFont,
    fontWeight = FontWeight.W600,
    fontSize = 22.sp
  ),
  h1 = TextStyle(
    fontFamily = MonteserratFont,
    fontWeight = FontWeight.W400,
  )
  /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)
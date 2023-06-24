package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography

@Composable
fun LoadingPage(message: String = "Carregando") {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(100.dp),
      color = Color.DarkGray,
      strokeWidth = 8.dp
    )
    Spacer(modifier = Modifier.padding(vertical = 20.dp))
    Text(
      text = message,
      style = Typography.h5.merge(
        TextStyle(
          color = Color.DarkGray,
          fontFamily = FontFamily.Serif
        )
      )
    )
  }
}

@Preview(showBackground = true)
@Composable
fun LoadingPagePreview() {
  AutoCareTheme() {
    LoadingPage()
  }
}
package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme

@Composable
fun LoadingPage(message: String = stringResource(R.string.text_placeholder_loading_page)) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(64.dp),
      color = MaterialTheme.colorScheme.primary,
      strokeWidth = 6.dp
    )
    Spacer(modifier = Modifier.padding(vertical = 16.dp))
    // FontFamily.Serif aqui era uma terceira familia solta na tela: o texto de
    // carregamento saia em serifada enquanto o app inteiro e Montserrat.
    Text(
      text = message,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
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
package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme

class MaintenanceActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        MaintenanceScreen()
      }
    }
  }
}

@Composable
fun MaintenanceScreen() {
  Scaffold(topBar = {
    TopAppBar(title = { Text(text = "Minhas manutenções") })
  }

  ) { contentPadding ->
    LazyColumn(modifier = Modifier.padding(contentPadding)) {

    }

  }
}

@Preview(showBackground = true)
@Composable
fun MaintenanceScreenPreview() {
  MaintenanceScreen()
}


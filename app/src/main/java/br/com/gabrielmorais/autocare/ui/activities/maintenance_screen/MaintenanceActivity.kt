package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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

}

@Preview(showBackground = true)
@Composable
fun MaintenanceScreenPreview() {
  MaintenanceScreen()
}


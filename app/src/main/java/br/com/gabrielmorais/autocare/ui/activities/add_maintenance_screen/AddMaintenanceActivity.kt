package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography

class AddMaintenanceActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme() {
        AddMaintenanceScreen()
      }
    }
  }
}

@Composable
fun AddMaintenanceScreen() {
  val state = AddMaintenanceUiState()
  Scaffold { paddingValues ->
    Column(
      Modifier
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {

      Spacer(modifier = Modifier.padding(top = 20.dp))
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.description,
        label = { Text(stringResource(id = R.string.text_description)) },
        onValueChange = state.onDescriptionChange
      )
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.date.toString(),
        label = { Text(stringResource(id = R.string.text_date)) },
        onValueChange = {}
      )
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = "",
        label = { Text(stringResource(id = R.string.text_current_mileage)) },
        onValueChange = {})
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = "",
        label = { Text(stringResource(id = R.string.text_next_maintenance_mileage)) },
        onValueChange = {})
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = "",
        label = { Text(stringResource(id = R.string.text_next_maintenance_months)) },
        onValueChange = {})
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = "",
        label = { Text(stringResource(id = R.string.text_comments)) },
        onValueChange = {}
      )
      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /*TODO*/ }) {
        Text(text = "Gravar", style = Typography.h5)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AddMaintenanceScreenPreview() {
  AddMaintenanceScreen()
}
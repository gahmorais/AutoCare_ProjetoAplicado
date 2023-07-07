package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddMaintenanceActivity : ComponentActivity() {
  private val viewModel: AddMaintenanceViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme() {
        AddMaintenanceScreen(viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddMaintenanceScreen(viewModel: AddMaintenanceViewModel) {
  val state = AddMaintenanceUiState()
  val services = viewModel.services.collectAsState()
  Scaffold { paddingValues ->
    Column(
      Modifier
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
      Spacer(modifier = Modifier.padding(top = 20.dp))
      if (services.value.isNotEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        var selectItem by remember { mutableStateOf(services.value[0]?.name) }
        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
        ) {
          TextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectItem.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "Tipo de serviço") },
            trailingIcon = {
              ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
          )
          ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }) {
            services.value.forEach { item ->
              DropdownMenuItem(onClick = {
                selectItem = item?.name
                expanded = false
              }) {
                Text(text = item?.name.toString())
              }
            }
          }
        }
      }

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
//  AddMaintenanceScreen()
}
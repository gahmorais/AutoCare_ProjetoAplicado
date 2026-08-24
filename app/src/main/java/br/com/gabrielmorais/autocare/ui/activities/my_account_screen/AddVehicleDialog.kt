package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.gabrielmorais.autocare.R

class AddVehicleDialogState {

  var nickName by mutableStateOf("")
    private set
  var brand by mutableStateOf("")
    private set
  var model by mutableStateOf("")
    private set
  var plate by mutableStateOf("")
    private set
  var averageDistanceTraveled by mutableStateOf("")
    private set
  var photo: String? by mutableStateOf(null)

  val onNickNameChange: (String) -> Unit = { newText ->
    nickName = newText
  }

  val onBrandChange: (String) -> Unit = { newText ->
    brand = newText
  }

  val onModelChange: (String) -> Unit = { newText ->
    model = newText
  }

  val onPlateChange: (String) -> Unit = { newText ->
    plate = newText
  }

  val onAverageDistanceChange: (String) -> Unit = { newText ->
    averageDistanceTraveled = newText
  }
}

@Composable
fun AddVehicleDialog(
  state: AddVehicleDialogState = AddVehicleDialogState(),
  onDismiss: () -> Unit = {},
  onConfirm: () -> Unit = {}
) {
  Dialog(
    onDismissRequest = {
      onDismiss()
    },
  ) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),) {
      Column(
        modifier = Modifier
          .clip(MaterialTheme.shapes.large)
          .padding(10.dp),
      ) {
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.titleMedium,
          label = { Text(text = stringResource(R.string.text_nickname)) },
          placeholder = { Text(text = stringResource(R.string.placeholder_nickname)) },
          value = state.nickName,
          onValueChange = state.onNickNameChange,
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.titleMedium,
          label = { Text(text = stringResource(R.string.text_brand)) },
          placeholder = { Text(text = stringResource(R.string.placeholder_brand)) },
          value = state.brand,
          onValueChange = state.onBrandChange
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.titleMedium,
          label = { Text(text = stringResource(R.string.text_model)) },
          placeholder = { Text(text = stringResource(R.string.placeholder_model)) },
          value = state.model,
          onValueChange = state.onModelChange
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.titleMedium,
          label = { Text(text = stringResource(R.string.text_plate)) },
          placeholder = { Text(text = stringResource(R.string.placeholder_plate)) },
          value = state.plate,
          onValueChange = state.onPlateChange
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = MaterialTheme.typography.titleMedium,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          label = { Text(text = stringResource(R.string.text_average_distace_traveled_per_month)) },
          placeholder = { Text(text = stringResource(R.string.placeholder_average_distance_traveled)) },
          value = state.averageDistanceTraveled,
          onValueChange = { state.onAverageDistanceChange(it.filter(Char::isDigit)) }
        )

        TextButton(
          modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
          onClick = { onConfirm() }
        ) {
          Text(text = stringResource(R.string.text_save), style = MaterialTheme.typography.titleLarge)
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AddVehicleDialogPreview() {
  Box(Modifier.fillMaxSize()) {
    AddVehicleDialog()
  }
}
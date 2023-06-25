package br.com.gabrielmorais.autocare.ui.activities.my_account_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import br.com.gabrielmorais.autocare.ui.theme.Typography

class AddVehicleDialogState {

  var nickName by mutableStateOf("")
    private set
  var brand by mutableStateOf("")
    private set
  var model by mutableStateOf("")
    private set
  var plate by mutableStateOf("")
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
    Card(elevation = 10.dp) {
      Column(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .padding(10.dp),
      ) {
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = Typography.h6,
          label = { Text(text = "Apelido") },
          placeholder = { Text(text = "Meu carro") },
          value = state.nickName,
          onValueChange = state.onNickNameChange,
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = Typography.h6,
          label = { Text(text = "Marca") },
          placeholder = { Text(text = "Honda") },
          value = state.brand,
          onValueChange = state.onBrandChange
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = Typography.h6,
          label = { Text(text = "Modelo") },
          placeholder = { Text(text = "Fit") },
          value = state.model,
          onValueChange = state.onModelChange
        )
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          textStyle = Typography.h6,
          label = { Text(text = "Placa") },
          placeholder = { Text(text = "abc1234") },
          value = state.plate,
          onValueChange = state.onPlateChange
        )

        TextButton(
          modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
          onClick = { onConfirm() }
        ) {
          Text(text = "Salvar", style = Typography.h5)
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
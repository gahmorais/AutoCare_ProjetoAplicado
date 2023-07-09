package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectMenu(
  modifier: Modifier = Modifier,
  expanded: Boolean = false,
  value: String = "",
  label: String = "",
  items: List<String>,
  onExpandedChange: (Boolean) -> Unit = {},
  onDissmis: () -> Unit = {},
  onClick: (String, Int) -> Unit,
  onChangeValue: (String) -> Unit = {}
) {
  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = onExpandedChange,
  ) {
    TextField(
      modifier = modifier,
      value = value,
      onValueChange = onChangeValue,
      readOnly = true,
      label = { Text(text = label) },
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
      }
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = onDissmis
    ) {
      items.forEachIndexed { i, item ->
        DropdownMenuItem(onClick = { onClick(item, i) }) {
          Text(text = item)
        }
      }
    }
  }
}
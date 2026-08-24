package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
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
      // menuAnchor e obrigatorio no M3: e ele que ancora o popup no campo.
      modifier = modifier.menuAnchor(),
      value = value,
      onValueChange = onChangeValue,
      readOnly = true,
      label = { Text(text = label) },
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
      },
      colors = ExposedDropdownMenuDefaults.textFieldColors()
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = onDissmis
    ) {
      items.forEachIndexed { i, item ->
        // No M3 o rotulo virou parametro `text` em vez de conteudo trailing.
        DropdownMenuItem(
          text = { Text(text = item) },
          onClick = { onClick(item, i) }
        )
      }
    }
  }
}
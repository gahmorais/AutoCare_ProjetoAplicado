package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PasswordTextField(
  modifier: Modifier = Modifier,
  label: String = "Senha",
  state: PasswordTextFieldState = PasswordTextFieldState()
) {

  OutlinedTextField(
    modifier = modifier,
    value = state.password,
    leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, null) },
    onValueChange = { state.onPasswordChange(it) },
    label = { Text(text = label) },
    visualTransformation = if (state.showPassword) {
      VisualTransformation.None
    } else PasswordVisualTransformation(),
    trailingIcon = {
      IconButton(onClick = { state.changePasswordVisibility(state.showPassword.not()) }) {
        Icon(
          imageVector = if (state.showPassword) {
            Icons.Outlined.Visibility
          } else Icons.Outlined.VisibilityOff,
          contentDescription = null
        )
      }
    }
  )
}

@Preview
@Composable
private fun PasswordTextFieldPreview(){
  PasswordTextField()
}


class PasswordTextFieldState {
  var password by mutableStateOf("")
  var showPassword by mutableStateOf(false)
  val onPasswordChange: (String) -> Unit = {
    password = it
  }
  val changePasswordVisibility: (Boolean) -> Unit = {
    showPassword = it
  }
}
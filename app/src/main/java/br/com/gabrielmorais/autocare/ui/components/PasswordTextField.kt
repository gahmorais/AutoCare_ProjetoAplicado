package br.com.gabrielmorais.autocare.ui.components

import android.opengl.Visibility
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import br.com.gabrielmorais.autocare.R

@Composable
fun PasswordTextField(
  modifier: Modifier = Modifier,
  label: String = stringResource(id = R.string.text_password),
  state: PasswordTextFieldState = PasswordTextFieldState()
) {

  OutlinedTextField(
    modifier = modifier,
    value = state.value,
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

@Composable
fun PasswordTextField(
  modifier: Modifier = Modifier,
  label: String = stringResource(id = R.string.text_password),
  value: String,
  onChangePassword: (String) -> Unit
) {

  var visibility by remember {
    mutableStateOf(false)
  }

  OutlinedTextField(
    modifier = modifier,
    value = value,
    leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, null) },
    onValueChange = { onChangePassword(it) },
    label = { Text(text = label) },
    visualTransformation = if (visibility) {
      VisualTransformation.None
    } else PasswordVisualTransformation(),
    trailingIcon = {
      IconButton(onClick = { visibility = visibility.not() }) {
        Icon(
          imageVector = if (visibility) {
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
private fun PasswordTextFieldPreview() {
  PasswordTextField()
}


class PasswordTextFieldState {
  var value by mutableStateOf("")
  var showPassword by mutableStateOf(false)
  val onPasswordChange: (String) -> Unit = {
    value = it
  }
  val changePasswordVisibility: (Boolean) -> Unit = {
    showPassword = it
  }
}
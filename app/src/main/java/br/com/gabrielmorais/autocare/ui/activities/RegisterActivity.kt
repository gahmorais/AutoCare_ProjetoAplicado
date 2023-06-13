package br.com.gabrielmorais.autocare.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { RegisterScreen() }

  }
}

@Composable
fun RegisterScreen() {
  val scaffoldState = rememberScaffoldState()
  AutoCareTheme {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      scaffoldState = scaffoldState,
      snackbarHost = { scaffoldState.snackbarHostState }
    ) { contentPadding ->
      Column(
        modifier = Modifier
          .padding(contentPadding)
          .padding(horizontal = 16.dp)
          .fillMaxSize()
      ) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          value = email,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          label = { Text(text = "Email") },
          leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
          placeholder = { Text(text = "email@email.com.br") },
          onValueChange = { email = it },
        )
        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
          value = password,
          label = { Text(text = "Senha") },
          visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, null) },
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = null
              )
            }
          },
          onValueChange = { password = it },
        )

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
          value = confirmPassword,
          label = { Text(text = "Confirme a senha") },
          visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          leadingIcon = { Icon(imageVector = Icons.Outlined.Lock, null) },
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = null
              )
            }
          },
          onValueChange = { confirmPassword = it },
        )

        TextButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            CoroutineScope(Dispatchers.Main).launch {
              scaffoldState.snackbarHostState.showSnackbar("Usuário cadastrado")
            }
          }) {
          Text(text = "Cadastrar", style = TextStyle(fontSize = 24.sp))
        }
        DefaultSnackBar(
          snackbarHostState = scaffoldState.snackbarHostState,
          onDismiss = {
            scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
          })
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
  RegisterScreen()
}
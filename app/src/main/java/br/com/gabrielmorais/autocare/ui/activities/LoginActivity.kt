package br.com.gabrielmorais.autocare.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.ui.authorization.Auth
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme

class LoginActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

      LoginScreen()
    }
  }

}

@Composable
fun LoginScreen() {
  AutoCareTheme {
    Surface {
      var email by remember { mutableStateOf("") }
      var password by remember { mutableStateOf("") }
      var showPassword by remember { mutableStateOf(false) }
      val context = LocalContext.current

      Column(
        Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp)
      ) {
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
        Row(modifier = Modifier.fillMaxWidth()) {
          TextButton(
            modifier = Modifier.fillMaxWidth(0.5f),
            onClick = {
              val intent = Intent(context, RegisterActivity::class.java)
              context.startActivity(intent)
            }) {

            Text(text = "Cadastrar", style = TextStyle(fontSize = 24.sp))
          }
          TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { Auth.getInstance().login(email, password) }) {
            Text(text = "Login", style = TextStyle(fontSize = 24.sp))
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
  LoginScreen()
}
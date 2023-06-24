package br.com.gabrielmorais.autocare.ui.activities.register_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.activities.register_screen.viewmodel.RegisterViewModel
import br.com.gabrielmorais.autocare.ui.activities.register_screen.viewmodel.RegisterViewModelFactory
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
  private val viewModel by viewModels<RegisterViewModel> {
    RegisterViewModelFactory(
      AuthRepositoryImpl(Firebase.auth),
      UserRepositoryImpl(Firebase.database)
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { RegisterScreen(viewModel) }
  }
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {

  val scaffoldState = rememberScaffoldState()
  val scope = rememberCoroutineScope()
  val state = viewModel.registerState.collectAsState(initial = null)
  val context = LocalContext.current as ComponentActivity
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  if (state.value?.isLoading == true) {
    LoadingPage("Registrando usuário")
  } else {
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
            .fillMaxSize(),
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
              viewModel.registerUser(email, password)
            }) {
            Text(text = "Cadastrar", style = TextStyle(fontSize = 24.sp))
          }
        }
        Box(
          Modifier.fillMaxSize(),
          contentAlignment = Alignment.BottomCenter
        ) {
          DefaultSnackBar(
            snackbarHostState = scaffoldState.snackbarHostState,
            onDismiss = {
              scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
            })
        }


      }
    }
  }

  LaunchedEffect(key1 = state.value?.isSuccess) {
    scope.launch {
      if (state.value?.isSuccess?.isNotEmpty() == true) {
        val success = state.value?.isSuccess
        Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
        context.finish()
      }
    }
  }

  LaunchedEffect(key1 = state.value?.isError) {
    scope.launch {
      if (state.value?.isError?.isNotEmpty() == true) {
        val error = state.value?.isError
        showSnackBar(scaffoldState, "$error")
      }
    }
  }
}

fun showSnackBar(scaffoldState: ScaffoldState, message: String) {
  CoroutineScope(Dispatchers.Main).launch {
    scaffoldState.snackbarHostState.showSnackbar(message)
  }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
//  RegisterScreen()
}
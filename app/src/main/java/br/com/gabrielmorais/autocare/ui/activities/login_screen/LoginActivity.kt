package br.com.gabrielmorais.autocare.ui.activities.login_screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainActivity
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterActivity
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.viewmodels.factory.LoginViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
  private val viewModel by viewModels<LoginViewModel> {
    LoginViewModelFactory(AuthRepositoryImpl(Firebase.auth))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LoginScreen(viewModel)
    }
  }

  override fun onStart() {
    super.onStart()

    viewModel.getCurrentUserListener()
    viewModel.currentUser?.let { user ->
      Log.i("LoginActivity", "onResume: ${viewModel.currentUser}")
      val openActivity = Intent(this, MainActivity::class.java)
      openActivity.putExtra("user_id", user.uid)
      startActivity(openActivity)
    }
  }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }
  val scaffoldState = rememberScaffoldState()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val state = viewModel.loginState.collectAsState(initial = null)
  if (state.value?.isLoading == true) {
    LoadingPage("Efetuando login")
  } else {
    AutoCareTheme {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { scaffoldState.snackbarHostState },
        scaffoldState = scaffoldState
      ) { contentPadding ->
        Column(
          Modifier
            .fillMaxSize()
            .padding(contentPadding)
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
              onClick = {
                viewModel.loginUser(email, password)
              }) {
              Text(text = "Login", style = TextStyle(fontSize = 24.sp))
            }
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
        val openActivity = Intent(context, MainActivity::class.java)
        context.startActivity(openActivity)
      }
    }
  }

  LaunchedEffect(key1 = state.value?.isError) {
    scope.launch {
      if (state.value?.isError?.isNotEmpty() == true) {
        val error = state.value?.isError
        Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
//  LoginScreen()
}
package br.com.gabrielmorais.autocare.ui.activities.login_screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainActivity
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterActivity
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import br.com.gabrielmorais.autocare.ui.components.PasswordTextField
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

    lifecycleScope.launch {
      viewModel.currentUser.collect { user ->
        user?.let {
          Log.i("LoginActivity", "onResume: ${viewModel.currentUser}")
          val openActivity = Intent(this@LoginActivity, MainActivity::class.java)
          openActivity.putExtra("user_id", it.uid)
          startActivity(openActivity)
          finish()
        }
      }
    }
  }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
  val scaffoldState = rememberScaffoldState()
  val context = LocalContext.current
  val stateUi = remember { viewModel.loginUiState }
  val passwordState = stateUi.passwordState
  val state = viewModel.loginState.collectAsState(initial = null)
  if (state.value?.isLoading == true) {
    LoadingPage(stringResource(id = R.string.text_loading_login))
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
            value = stateUi.email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            label = { Text(text = stringResource(id = R.string.text_email)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null
              )
            },
            placeholder = {
              Text(text = stringResource(id = R.string.email_placeholder))
            },
            onValueChange = stateUi.onEmailChange,
          )

          PasswordTextField(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 32.dp),
            state = passwordState
          )

          Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
              modifier = Modifier.fillMaxWidth(0.5f),
              onClick = {
                val intent = Intent(context, RegisterActivity::class.java)
                context.startActivity(intent)
              }) {
              Text(
                text = stringResource(id = R.string.text_register),
                style = TextStyle(fontSize = 24.sp)
              )
            }
            TextButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = {
                viewModel.loginUser(stateUi.email, passwordState.password)
              }) {
              Text(
                text = stringResource(id = R.string.text_login),
                style = TextStyle(fontSize = 24.sp)
              )
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

  LaunchedEffect(key1 = state.value?.isError) {
    launch {
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
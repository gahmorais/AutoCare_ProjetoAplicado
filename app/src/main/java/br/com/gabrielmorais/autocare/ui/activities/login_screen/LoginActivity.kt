package br.com.gabrielmorais.autocare.ui.activities.login_screen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainActivity
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterActivity
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import br.com.gabrielmorais.autocare.ui.components.PasswordTextField
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_USER_ID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class LoginActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LoginScreen()
    }
  }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
  val scaffoldState = rememberScaffoldState()
  val context = LocalContext.current as LoginActivity
  val stateUi by viewModel.loginUiState.collectAsState()
  val passwordState = stateUi.passwordState
  val state by viewModel.loginState.collectAsState(initial = null)
  val scope = rememberCoroutineScope()
  if (state?.isLoading == true) {
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
          Image(
            painter = painterResource(id = R.drawable.logo_autocare),
            modifier = Modifier
              .fillMaxWidth()
              .fillMaxHeight(0.4F),
            contentDescription = null
          )
          OutlinedTextField(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            value = stateUi.nickname,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            label = { Text(text = stringResource(id = R.string.text_nickname)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null
              )
            },
            placeholder = {
              Text(text = stringResource(id = R.string.email_placeholder))
            },
            onValueChange = stateUi.onNickNameChange,
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
                scope.launch {
                  viewModel.loginUser(stateUi.nickname, passwordState.value)
                }
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

  LaunchedEffect(key1 = state?.isError) {
    if (state?.isError?.isNotEmpty() == true) {
      val error = state?.isError
      Timber.tag("LoginActivity").i("Ocorreu um erro $error")
      Toast.makeText(context, "$error", Toast.LENGTH_SHORT).show()
    }
  }

  val currentUser by viewModel.currentUser.collectAsState()
  LaunchedEffect(key1 = currentUser) {
    if (currentUser != null) {
      Timber.tag("LoginActivity").i("onResume: ${viewModel.currentUser.value}")
      val openActivity = Intent(context, MainActivity::class.java)
      openActivity.putExtra(INTENT_USER_ID, currentUser?.id)
      val bundle = Bundle()
      bundle.putString(INTENT_USER_ID, currentUser?.id)
      startActivity(context, openActivity, bundle)
      context.finish()
    }
  }
}
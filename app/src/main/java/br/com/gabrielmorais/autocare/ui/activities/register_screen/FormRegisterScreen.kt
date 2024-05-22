package br.com.gabrielmorais.autocare.ui.activities.register_screen

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.components.PasswordTextField
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun FormRegisterScreen(viewModel: RegisterViewModel = koinViewModel()) {
  val scaffoldState = rememberScaffoldState()
  val uiState = RegisterUiState()
  val state = viewModel.registerState.collectAsState(initial = null)
  val context = LocalContext.current as ComponentActivity
  var message by remember { mutableStateOf("") }
  AutoCareTheme {
    Scaffold(
      modifier = Modifier.fillMaxSize(),
      scaffoldState = scaffoldState,
      snackbarHost = { scaffoldState.snackbarHostState }
    ) { contentPadding ->
      Column(
        modifier = Modifier
          .padding(contentPadding)
          .padding(horizontal = 32.dp)
          .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
      ) {

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth(),
          value = uiState.nickname,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
          label = { Text(text = stringResource(id = R.string.text_username)) },
          leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
          placeholder = { Text(text = stringResource(R.string.placeholder_username)) },
          onValueChange = uiState.onNickNameChange,
        )

        OutlinedTextField(
          modifier = Modifier
            .fillMaxWidth(),
          value = uiState.name,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
          label = { Text(text = stringResource(id = R.string.text_name)) },
          leadingIcon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
          placeholder = { Text(text = stringResource(R.string.placeholder_name)) },
          onValueChange = uiState.onNameChange,
        )

        PasswordTextField(
          modifier = Modifier.fillMaxWidth(),
          label = stringResource(id = R.string.text_password),
          value = uiState.password,
          onChangePassword = uiState.onPasswordChange
        )

        PasswordTextField(
          modifier = Modifier.fillMaxWidth(),
          label = stringResource(id = R.string.text_confirm_password),
          value = uiState.confirmPassword,
          onChangePassword = uiState.onConfirmPasswordChange
        )

        TextButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            val (isValid, text) = uiState.allFieldsAreValid()
            if (!isValid) {
              message = text
              return@TextButton
            }
            val user = User(
              nickname = uiState.nickname,
              name = uiState.name,
              password = uiState.password
            )
            viewModel.registerUser(user)

          }) {
          Text(
            text = stringResource(R.string.text_register_user),
            style = TextStyle(fontSize = 24.sp)
          )
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

    LaunchedEffect(key1 = message) {
      if (message.isNotEmpty()) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        message = ""
      }
    }

    LaunchedEffect(key1 = state.value?.isSuccess) {
      if (state.value?.isSuccess?.isNotEmpty() == true) {
        val success = state.value?.isSuccess
        Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
        context.finish()
      }
    }

    LaunchedEffect(key1 = state.value?.isError) {
      if (state.value?.isError?.isNotEmpty() == true) {
        val error = state.value?.isError ?: "Ocorreu um erro"
        scaffoldState.snackbarHostState.showSnackbar(error)
      }
    }
  }
}

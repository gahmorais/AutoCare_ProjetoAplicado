package br.com.gabrielmorais.autocare.ui.activities.register_screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainActivity
import br.com.gabrielmorais.autocare.ui.components.DefaultSnackBar
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import br.com.gabrielmorais.autocare.ui.components.PasswordTextField
import br.com.gabrielmorais.autocare.ui.components.PasswordTextFieldState
import br.com.gabrielmorais.autocare.utils.Constants
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class RegisterActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { RegisterScreen() }
  }
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = koinViewModel()) {
  val state by viewModel.registerState.collectAsState(initial = null)
  if (state?.isLoading == true) {
    LoadingPage(stringResource(R.string.text_loading_register_user))
  } else {
    FormRegisterScreen()
  }

  val context = LocalContext.current as RegisterActivity
  LaunchedEffect(key1 = state?.isSuccess) {
    if (state?.isSuccess?.isNotEmpty() == true) {
      val success = state?.isSuccess
      Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
      context.finish()
    }
  }

}
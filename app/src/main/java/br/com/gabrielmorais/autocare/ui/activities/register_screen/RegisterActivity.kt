package br.com.gabrielmorais.autocare.ui.activities.register_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import org.koin.androidx.compose.koinViewModel

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
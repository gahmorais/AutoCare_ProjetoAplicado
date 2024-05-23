package br.com.gabrielmorais.autocare.ui.activities.register_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import br.com.gabrielmorais.autocare.data.repository.Status
import org.koin.androidx.compose.koinViewModel

class RegisterActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { RegisterScreen() }
  }
}

@Composable
fun RegisterScreen(viewModel: RegisterViewModel = koinViewModel()) {
  FormRegisterScreen()

  val context = LocalContext.current as RegisterActivity
  val state by viewModel.registerState.collectAsState(null)

  if (state?.status == Status.SUCCESS) {
    val success = state?.message
    Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
    context.finish()
  }

  if (state?.status == Status.ERROR) {
    val success = state?.message
    Toast.makeText(context, "$success", Toast.LENGTH_SHORT).show()
  }
}
package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.data.notifications.BootReceiver
import br.com.gabrielmorais.autocare.ui.activities.login_screen.LoginActivity
import br.com.gabrielmorais.autocare.ui.navigation.AutoCareApp
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A unica Activity do app depois da fase 3. Antes eram quatro telas ligadas por
 * Intent - inicial, minha conta, detalhe do veiculo e formulario de manutencao -
 * duas delas sem botao de voltar. Login e cadastro seguem como Activities
 * proprias: sao o fluxo anterior a ter sessao, e nao compartilham nem a barra
 * inferior nem o ViewModel da casca.
 */
class MainActivity : ComponentActivity() {
  private val viewModel by viewModel<MainViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
      )
      // Pedir a permissao no corpo da composicao e efeito colateral: era
      // disparado de novo a cada recomposicao. LaunchedEffect(Unit) roda uma vez.
      LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.POST_NOTIFICATIONS
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }

      AutoCareTheme {
        AutoCareApp(
          shellViewModel = viewModel,
          onLoggedOut = ::backToLogin
        )
      }
    }

    // Cobre o caso de o BOOT_COMPLETED nao ter chegado (app forcado a parar,
    // instalacao nova) mantendo os alarmes consistentes ao abrir o app.
    BootReceiver.enqueueReschedule(this)

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        if (message.isNotBlank()) {
          Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
      }
    }

    // Uma vez so: o Flow do repositorio mantem as tres abas atualizadas sozinho.
    // Antes isso rodava em onResume e cada retorno a tela deixava para tras
    // mais um ValueEventListener ativo.
    viewModel.observeUser()
  }

  /**
   * LoginActivity ja tinha se finalizado ao abrir a Main, entao o finish()
   * sozinho esvaziava a pilha e fechava o app em vez de voltar ao login.
   */
  private fun backToLogin() {
    val intent = Intent(this, LoginActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    finish()
  }
}

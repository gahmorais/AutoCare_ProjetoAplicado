package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.Manifest
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.data.notifications.BootReceiver
import br.com.gabrielmorais.autocare.ui.navigation.AutoCareApp
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A unica Activity do app e a porta de entrada: sem login, abre direto no
 * primeiro acesso (sem perfil) ou nas abas (com perfil).
 */
class MainActivity : ComponentActivity() {
  private val viewModel by viewModel<MainViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Antes do super: a splash precisa estar instalada antes de a janela existir.
    // Ela segura a tela ate o banco dizer se ha perfil - sem isso o onboarding
    // piscaria para quem ja usa o app.
    installSplashScreen().setKeepOnScreenCondition {
      viewModel.startState.value == StartState.LOADING
    }

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
        AutoCareApp(shellViewModel = viewModel)
      }
    }

    // Reagenda os lembretes e recolhe fotos sem dono. Cobre o caso de o
    // BOOT_COMPLETED nao ter chegado (app forcado a parar, instalacao nova,
    // backup restaurado).
    BootReceiver.enqueueReschedule(this)

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        if (message.isNotBlank()) {
          Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }
}

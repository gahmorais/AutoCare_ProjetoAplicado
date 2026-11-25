package br.com.gabrielmorais.autocare.ui.activities.onboarding_screen

import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsets
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.ui.components.ProfileAvatarPicker

/**
 * Primeiro acesso: cria o primeiro perfil. So o nome e obrigatorio - a foto
 * pode ser posta agora ou depois, na aba Perfil.
 *
 * Sem estado de ViewModel: recebe o resultado por [onStart], o que deixa a tela
 * testavel isolada.
 */
@Composable
fun OnboardingScreen(onStart: (name: String, photo: Uri?) -> Unit) {
  var name by rememberSaveable { mutableStateOf("") }
  var photo by rememberSaveable { mutableStateOf<Uri?>(null) }
  val canStart = name.isNotBlank()

  Scaffold(
    // safeDrawing inclui o teclado: com edge-to-edge a janela nao encolhe
    // sozinha, e o campo em foco ficaria escondido atras dele.
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
      Text(
        modifier = Modifier.semantics { heading() },
        text = stringResource(R.string.onboarding_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center
      )
      Text(
        text = stringResource(R.string.onboarding_body),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      ProfileAvatarPicker(photo = photo, onPhotoPicked = { photo = it })
      Text(
        text = stringResource(R.string.onboarding_photo_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.text_name)) },
        placeholder = { Text(stringResource(R.string.name_placeholder)) },
        singleLine = true,
        value = name,
        onValueChange = { name = it },
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { if (canStart) onStart(name.trim(), photo) })
      )

      Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = canStart,
        onClick = { onStart(name.trim(), photo) }
      ) {
        Text(stringResource(R.string.onboarding_start))
      }
    }
  }
}

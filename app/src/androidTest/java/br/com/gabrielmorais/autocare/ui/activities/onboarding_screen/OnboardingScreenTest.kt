package br.com.gabrielmorais.autocare.ui.activities.onboarding_screen

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.gabrielmorais.autocare.data.backup.BackupPreview
import br.com.gabrielmorais.autocare.ui.activities.profile_screen.BackupOperation
import br.com.gabrielmorais.autocare.ui.activities.profile_screen.BackupUiState
import br.com.gabrielmorais.autocare.ui.activities.profile_screen.RestoreFromFileContent
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

  @get:Rule
  val compose = createComposeRule()

  private var started: Pair<String, Uri?>? = null

  private val eventos = mutableListOf<String>()

  private fun renderizar(restore: BackupUiState = BackupUiState()) {
    compose.setContent {
      AutoCareTheme {
        OnboardingScreen(
          onStart = { name, photo -> started = name to photo },
          restoreAction = {
            RestoreFromFileContent(
              state = restore,
              onImport = { eventos += "importar" },
              onConfirmImport = { eventos += "confirmar" },
              onCancelImport = { eventos += "cancelar" },
              onDismissResult = { eventos += "fechar" }
            )
          }
        )
      }
    }
  }

  @Test
  fun comecar_fica_desabilitado_sem_nome() {
    renderizar()

    compose.onNodeWithText("Começar").assertIsNotEnabled()
    compose.onNodeWithText("Nome").performTextInput("   ")
    compose.onNodeWithText("Começar").assertIsNotEnabled()
  }

  @Test
  fun comecar_entrega_o_nome_sem_espacos_e_sem_foto() {
    renderizar()

    compose.onNodeWithText("Nome").performTextInput("  Maria ")
    compose.onNodeWithText("Começar").assertIsEnabled().performClick()

    assertEquals("Maria", started?.first)
    assertNull("foto é opcional", started?.second)
  }

  @Test
  fun explica_que_os_dados_ficam_no_aparelho() {
    renderizar()

    compose.onNodeWithText("Bem-vindo ao AutoCare").assertExists()
    compose.onNodeWithText(
      "Como você se chama? Seus veículos e manutenções ficam salvos só neste aparelho, sem cadastro nem senha."
    ).assertExists()
  }

  @Test
  fun restaurar_de_um_arquivo_abre_o_seletor_sem_criar_perfil() {
    renderizar()

    compose.onNodeWithText("Restaurar de um arquivo").performScrollTo().assertIsEnabled().performClick()

    assertEquals(listOf("importar"), eventos)
    assertNull("restaurar nao cria perfil", started)
  }

  @Test
  fun restaurar_mostra_a_previa_antes_de_importar() {
    renderizar(BackupUiState(preview = BackupPreview(newProfiles = 1, newVehicles = 2)))

    compose.onNodeWithText("Importar este backup?").assertIsDisplayed()
    compose.onNodeWithText("Importar").performClick()

    assertEquals(listOf("confirmar"), eventos)
  }

  @Test
  fun restaurar_fica_desabilitado_enquanto_le_o_arquivo() {
    renderizar(BackupUiState(operation = BackupOperation.READING))

    compose.onNodeWithText("Restaurar de um arquivo").assertIsNotEnabled()
    assertTrue(eventos.isEmpty())
  }
}

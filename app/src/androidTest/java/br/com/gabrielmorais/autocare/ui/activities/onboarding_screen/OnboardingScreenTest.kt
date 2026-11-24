package br.com.gabrielmorais.autocare.ui.activities.onboarding_screen

import android.net.Uri
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {

  @get:Rule
  val compose = createComposeRule()

  private var started: Pair<String, Uri?>? = null

  private fun renderizar() {
    compose.setContent {
      AutoCareTheme {
        OnboardingScreen(onStart = { name, photo -> started = name to photo })
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
}

package br.com.gabrielmorais.autocare.ui.activities.maintenance_screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.Month

/**
 * A regua e o elemento central do redesenho, e ate aqui so havia prova de que
 * compila. Este teste renderiza os tres estados de verdade e verifica que cada
 * selo aparece - e de quebra grava um PNG para inspecao visual.
 */
@RunWith(AndroidJUnit4::class)
class SimpleCardMaintenanceTest {

  @get:Rule
  val compose = createComposeRule()

  private val feitoEm = LocalDate.of(2025, Month.JANUARY, 1)
  private val media = 1_000

  private fun manutencao(id: Int, descricao: String, completed: Boolean = false) = Maintenance(
    id = id,
    description = descricao,
    date = feitoEm.toEpochDay(),
    currentMileage = 75_000,
    forecastNextExchangeMileage = 82_000,
    forecastNextExchangeDate = feitoEm.plusMonths(7).toEpochDay(),
    comments = "Óleo ELF 10W40 semissintético",
    completed = completed
  )

  private fun renderizarOsQuatroEstados() {
    compose.setContent {
      AutoCareTheme {
        Column(
          modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // "hoje" fixo em cada card: sem isso o teste mudaria de resultado
          // conforme a data em que roda.
          SimpleCardMaintenance(
            modifier = Modifier.fillMaxWidth(),
            maintenance = manutencao(1, "Filtro de ar"),
            averageDistancePerMonth = media,
            today = feitoEm.plusMonths(1)
          )
          SimpleCardMaintenance(
            modifier = Modifier.fillMaxWidth(),
            maintenance = manutencao(2, "Alinhamento"),
            averageDistancePerMonth = media,
            today = feitoEm.plusMonths(6).plusDays(15)
          )
          SimpleCardMaintenance(
            modifier = Modifier.fillMaxWidth(),
            maintenance = manutencao(3, "Troca de óleo"),
            averageDistancePerMonth = media,
            today = feitoEm.plusMonths(9)
          )
          SimpleCardMaintenance(
            modifier = Modifier.fillMaxWidth(),
            maintenance = manutencao(4, "Pastilhas de freio", completed = true),
            averageDistancePerMonth = media,
            today = feitoEm.plusMonths(9)
          )
        }
      }
    }
  }

  @Test
  fun cada_estado_mostra_o_proprio_selo() {
    renderizarOsQuatroEstados()

    compose.onNodeWithText("Em dia").assertIsDisplayed()
    compose.onNodeWithText("Vence em breve").assertIsDisplayed()
    compose.onNodeWithText("Vencida").assertIsDisplayed()
    compose.onNodeWithText("Concluída").assertIsDisplayed()
  }

  @Test
  fun a_regua_mostra_as_pontas_e_a_estimativa() {
    renderizarOsQuatroEstados()

    // useUnmergedTree porque o card e mergeado para o TalkBack anunciar como
    // uma unidade so - na arvore mesclada os textos nao existem separados.
    // Separador de milhar: a versao anterior imprimia "75000" cru.
    compose.onAllNodesWithText("75.000", useUnmergedTree = true)
      .onFirst().assertIsDisplayed()
    compose.onAllNodesWithText("82.000", useUnmergedTree = true)
      .onFirst().assertIsDisplayed()
    // 31 dias / 30,44 x 1.000 = 1.018 km alem dos 75.000
    compose.onNodeWithText("≈76.018 km", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun captura_para_inspecao_visual() {
    renderizarOsQuatroEstados()

    // Diretorio proprio do app em vez de /sdcard: sempre gravavel, sem depender
    // de permissao de armazenamento nem da versao do Android.
    val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    val destino = File(contexto.getExternalFilesDir(null), "autocare_regua.png")

    val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
    destino.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    Log.i("SimpleCardMaintenanceTest", "captura em ${destino.absolutePath}")
  }
}

package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A tela abria em branco e fechava sozinha: `return@Column` dentro da lambda do
 * Column deixava grupos abertos no Composer sem o endNode correspondente, e a
 * composicao morria com IndexOutOfBoundsException em ComposerImpl.endNode.
 *
 * O estado inicial e o de carregamento, que era justamente um dos ramos com
 * retorno antecipado - entao abrir a Activity ja basta para exercitar a
 * regressao, sem precisar de dados no Firebase.
 */
@RunWith(AndroidJUnit4::class)
class AddMaintenanceActivityTest {

  @Test
  fun a_tela_compoe_sem_derrubar_o_composer() {
    ActivityScenario.launch(AddMaintenanceActivity::class.java).use { scenario ->
      scenario.moveToState(Lifecycle.State.RESUMED)

      assertEquals(Lifecycle.State.RESUMED, scenario.state)
    }
  }
}

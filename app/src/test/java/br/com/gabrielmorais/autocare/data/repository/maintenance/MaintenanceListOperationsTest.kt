package br.com.gabrielmorais.autocare.data.repository.maintenance

import br.com.gabrielmorais.autocare.data.models.Maintenance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * As duas operacoes de lista sao puras justamente para poderem ser verificadas
 * sem Firebase: o repositorio so as envolve no ciclo ler-gravar.
 */
class MaintenanceListOperationsTest {

  private val pendente = Maintenance(id = 1, description = "Troca de óleo")
  private val outra = Maintenance(id = 2, description = "Alinhamento")

  @Test
  fun `replacingById troca apenas a manutencao alvo e preserva a posicao`() {
    val lista = listOf(pendente, outra)

    val atualizada = lista.replacingById(pendente.copy(completed = true))

    assertEquals(listOf(pendente.copy(completed = true), outra), atualizada)
  }

  @Test
  fun `replacingById devolve null quando o id nao esta na lista`() {
    val lista = listOf(pendente, outra)

    assertNull(lista.replacingById(Maintenance(id = 99)))
  }

  @Test
  fun `removingById tira o item e mantem os demais`() {
    val lista = listOf(pendente, outra)

    assertEquals(listOf(outra), lista.removingById(pendente.id))
  }

  @Test
  fun `removingById devolve null quando o id nao esta na lista`() {
    assertNull(listOf(pendente).removingById(99))
  }
}

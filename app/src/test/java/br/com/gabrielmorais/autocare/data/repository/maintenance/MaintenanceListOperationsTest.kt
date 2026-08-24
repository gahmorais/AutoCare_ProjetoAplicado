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

  @Test
  fun `addingIfAbsent devolve a manutencao ao fim da lista`() {
    val lista = listOf(outra)

    assertEquals(listOf(outra, pendente), lista.addingIfAbsent(pendente))
  }

  @Test
  fun `addingIfAbsent nao duplica quando ja esta na lista`() {
    // O desfazer do snackbar pode ser tocado duas vezes, e duplicar o registro
    // quebraria a chave da lista e o requestCode do alarme, que sao o mesmo id.
    val lista = listOf(pendente, outra)

    assertNull(lista.addingIfAbsent(pendente))
  }

  @Test
  fun `excluir e restaurar devolve a lista ao estado original`() {
    val original = listOf(pendente, outra)

    val depoisDeExcluir = original.removingById(pendente.id)!!
    val depoisDeRestaurar = depoisDeExcluir.addingIfAbsent(pendente)!!

    assertEquals(original.toSet(), depoisDeRestaurar.toSet())
  }
}

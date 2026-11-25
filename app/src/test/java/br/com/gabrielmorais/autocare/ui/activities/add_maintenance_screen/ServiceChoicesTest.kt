package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import br.com.gabrielmorais.autocare.data.models.Service
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ServiceChoicesTest {

  private val catalog = catalogOptions(
    listOf(
      Service(name = "Aditivo do radiador", mileageChange = 40_000, mustBeDoneBefore = 24),
      Service(name = "Óleo de motor", mileageChange = 10_000, mustBeDoneBefore = 12)
    )
  )

  @Test
  fun `catalogo descarta servicos incompletos`() {
    val options = catalogOptions(
      listOf(
        Service(name = "Óleo de motor", mileageChange = 10_000, mustBeDoneBefore = 12),
        Service(name = "Sem intervalo"),
        Service(mileageChange = 5_000, mustBeDoneBefore = 6)
      )
    )

    assertEquals(listOf("Óleo de motor"), options.map { it.name })
    assertTrue(options.single().fromCatalog)
  }

  @Test
  fun `criacao comeca no primeiro servico do catalogo`() {
    val choices = serviceChoices(catalog, editingDescription = null)

    assertEquals(catalog, choices.options)
    assertEquals(0, choices.initialIndex)
  }

  @Test
  fun `descricao do catalogo comeca selecionada`() {
    val choices = serviceChoices(catalog, "Óleo de motor")

    assertEquals(catalog, choices.options)
    assertEquals(1, choices.initialIndex)
  }

  @Test
  fun `descricao fora do catalogo vira a primeira opcao e comeca selecionada`() {
    val choices = serviceChoices(catalog, "Troca de óleo")

    assertEquals(listOf("Troca de óleo") + catalog.map { it.name }, choices.options.map { it.name })
    assertEquals(0, choices.initialIndex)

    val outside = choices.options[0]
    assertFalse(outside.fromCatalog)
    assertNull(outside.mileageChange)
    assertNull(outside.mustBeDoneBefore)
  }

  @Test
  fun `descricao em branco cai no primeiro servico sem opcao extra`() {
    val choices = serviceChoices(catalog, "  ")

    assertEquals(catalog, choices.options)
    assertEquals(0, choices.initialIndex)
  }
}

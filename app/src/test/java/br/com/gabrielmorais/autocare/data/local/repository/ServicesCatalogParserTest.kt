package br.com.gabrielmorais.autocare.data.local.repository

import br.com.gabrielmorais.autocare.data.models.Service
import org.json.JSONException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ServicesCatalogParserTest {

  @Test
  fun `le nome, quilometragem e prazo de cada servico`() {
    val json = """
      [
        { "name": "Troca de óleo", "mileageChange": 10000, "mustBeDoneBefore": 12 },
        { "name": " Fluido de freio ", "mileageChange": 30000, "mustBeDoneBefore": 24 }
      ]
    """
    assertEquals(
      listOf(
        Service("Troca de óleo", 10_000, 12),
        Service("Fluido de freio", 30_000, 24)
      ),
      ServicesCatalogParser.parse(json)
    )
  }

  @Test
  fun `lista vazia e valida`() {
    assertEquals(emptyList<Service>(), ServicesCatalogParser.parse("[]"))
  }

  @Test
  fun `json invalido falha`() {
    assertThrows(JSONException::class.java) { ServicesCatalogParser.parse("[{") }
  }

  @Test
  fun `campo ausente falha`() {
    assertThrows(JSONException::class.java) {
      ServicesCatalogParser.parse("""[{ "name": "Óleo", "mileageChange": 10000 }]""")
    }
  }

  @Test
  fun `prazo zero e aceito e significa sem prazo por tempo`() {
    val json = """[{ "name": "Velas", "mileageChange": 40000, "mustBeDoneBefore": 0 }]"""
    assertEquals(listOf(Service("Velas", 40_000, 0)), ServicesCatalogParser.parse(json))
  }

  @Test
  fun `nome vazio, quilometragem nao positiva ou prazo negativo falham`() {
    listOf(
      """[{ "name": "  ", "mileageChange": 10000, "mustBeDoneBefore": 12 }]""",
      """[{ "name": "Óleo", "mileageChange": 0, "mustBeDoneBefore": 12 }]""",
      """[{ "name": "Óleo", "mileageChange": 10000, "mustBeDoneBefore": -1 }]"""
    ).forEach { json ->
      assertThrows(json, IllegalArgumentException::class.java) { ServicesCatalogParser.parse(json) }
    }
  }

  /**
   * Valida o arquivo que vai no APK. Testes unitarios rodam com o modulo app
   * como diretorio de trabalho, entao o caminho relativo alcanca src/main.
   */
  @Test
  fun `catalogo embarcado e valido e sem nomes repetidos`() {
    val file = File("src/main/assets/${AssetServicesRepository.FILE_NAME}")
    assertTrue("catálogo não encontrado em ${file.absolutePath}", file.exists())

    val services = ServicesCatalogParser.parse(file.readText())

    assertTrue("o catálogo não pode estar vazio", services.isNotEmpty())
    val names = services.map { it.name!!.lowercase() }
    assertEquals("nomes repetidos no catálogo", names.size, names.toSet().size)
    // O no do Firebase tinha uma entrada "Teste" de desenvolvimento; ela nao
    // pode voltar para o catalogo que vai ao usuario.
    assertTrue("entrada de teste no catálogo", names.none { it == "teste" })
  }
}

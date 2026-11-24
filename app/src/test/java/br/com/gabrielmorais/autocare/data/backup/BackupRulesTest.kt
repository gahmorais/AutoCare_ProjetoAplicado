package br.com.gabrielmorais.autocare.data.backup

import br.com.gabrielmorais.autocare.data.images.ImagePaths
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * As regras de backup sao XML e nao enxergam o codigo: renomear o banco ou a
 * pasta de fotos tiraria os dados do backup sem nenhum erro de build. Este teste
 * amarra os caminhos das regras as constantes que o app usa de verdade.
 *
 * Testes unitarios rodam com o modulo app como diretorio de trabalho.
 */
class BackupRulesTest {

  private val expected = setOf(
    "database" to AutoCareDatabase.NAME,
    "database" to "${AutoCareDatabase.NAME}-wal",
    "database" to "${AutoCareDatabase.NAME}-shm",
    "file" to "${ImagePaths.DIR}/",
    // preferencesDataStore grava em files/datastore/<nome>.preferences_pb.
    "file" to "datastore/"
  )

  private fun rules(path: String, section: String? = null): List<Pair<String, String>> {
    val file = File("src/main/res/xml/$path")
    assertTrue("arquivo não encontrado: ${file.absolutePath}", file.exists())
    val root = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement
    val scope = section?.let { root.getElementsByTagName(it).item(0) as Element } ?: root
    val includes = scope.getElementsByTagName("include")
    assertEquals("regras não podem ter <exclude>: só entra o que for incluído", 0, scope.getElementsByTagName("exclude").length)
    return List(includes.length) { index ->
      val element = includes.item(index) as Element
      element.getAttribute("domain") to element.getAttribute("path")
    }
  }

  @Test
  fun `android 12 ou mais faz backup do banco, das fotos e do perfil ativo`() {
    assertEquals(expected, rules("data_extraction_rules.xml", "cloud-backup").toSet())
  }

  @Test
  fun `transferencia entre aparelhos leva o mesmo conteudo do backup`() {
    assertEquals(expected, rules("data_extraction_rules.xml", "device-transfer").toSet())
  }

  @Test
  fun `android 9 a 11 usa as mesmas regras`() {
    assertEquals(expected, rules("backup_rules.xml").toSet())
  }
}

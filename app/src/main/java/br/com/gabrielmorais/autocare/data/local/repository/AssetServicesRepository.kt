package br.com.gabrielmorais.autocare.data.local.repository

import android.content.res.AssetManager
import br.com.gabrielmorais.autocare.data.models.Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

/**
 * Catalogo de servicos embarcado no APK (assets/services.json), no lugar do no
 * "lista-servicos" do Firebase. Lido uma vez e mantido em memoria.
 */
class AssetServicesRepository(
  private val assets: AssetManager,
  private val fileName: String = FILE_NAME
) : ServicesRepository {
  private val mutex = Mutex()
  private var cache: List<Service>? = null

  override suspend fun getServices(): List<Service> = mutex.withLock {
    cache ?: withContext(Dispatchers.IO) {
      assets.open(fileName).bufferedReader().use { ServicesCatalogParser.parse(it.readText()) }
    }.also { cache = it }
  }

  companion object {
    const val FILE_NAME = "services.json"
  }
}

/**
 * Separado do repositorio para ser testavel na JVM: o teste roda com o org.json
 * real no classpath, e nao com o stub do android.jar.
 */
object ServicesCatalogParser {
  /**
   * Formato: lista de objetos {name, mileageChange, mustBeDoneBefore}, com a
   * quilometragem em km e o prazo em meses. Prazo 0 e valido e e o caso comum:
   * significa "sem prazo minimo por tempo" - o formulario usa
   * maxOf(meses pela quilometragem, mustBeDoneBefore), entao a data prevista
   * sai so da media mensal rodada.
   *
   * Estrito de proposito: o arquivo e nosso e vai no APK, entao um item
   * incompleto e bug de quem editou, e o teste do catalogo real pega antes do
   * build chegar ao usuario.
   *
   * @throws org.json.JSONException se o JSON for invalido ou faltar campo.
   * @throws IllegalArgumentException se o nome for vazio, a quilometragem nao
   * for positiva ou o prazo for negativo.
   */
  fun parse(json: String): List<Service> {
    val array = JSONArray(json)
    return List(array.length()) { index ->
      val item = array.getJSONObject(index)
      val name = item.getString("name").trim()
      val mileage = item.getInt("mileageChange")
      val months = item.getInt("mustBeDoneBefore")
      require(name.isNotEmpty()) { "Serviço $index sem nome" }
      require(mileage > 0) { "Serviço '$name' com quilometragem inválida: $mileage" }
      require(months >= 0) { "Serviço '$name' com prazo inválido: $months" }
      Service(name = name, mileageChange = mileage, mustBeDoneBefore = months)
    }
  }
}

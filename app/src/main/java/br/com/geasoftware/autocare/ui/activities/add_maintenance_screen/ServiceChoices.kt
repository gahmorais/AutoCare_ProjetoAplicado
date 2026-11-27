package br.com.geasoftware.autocare.ui.activities.add_maintenance_screen

import br.com.geasoftware.autocare.data.models.Service

/**
 * Uma opcao do menu de tipo de servico.
 *
 * As do catalogo ja chegam validadas (os campos do modelo sao nulaveis e um
 * registro incompleto derrubava a tela inteira). A opcao fora do catalogo e a
 * descricao de uma manutencao existente que nao esta no catalogo, por exemplo
 * vinda de um backup importado: ela nao tem intervalo, entao [mileageChange] e
 * [mustBeDoneBefore] sao nulos e as previsoes nao sao recalculadas a partir dela.
 */
internal data class ServiceOption(
  val name: String,
  val mileageChange: Int?,
  val mustBeDoneBefore: Int?
) {
  val fromCatalog: Boolean get() = mileageChange != null && mustBeDoneBefore != null
}

internal data class ServiceChoices(
  val options: List<ServiceOption>,
  val initialIndex: Int
)

/** Servicos do catalogo com todos os campos preenchidos; os incompletos saem. */
internal fun catalogOptions(services: List<Service>): List<ServiceOption> =
  services.mapNotNull { service ->
    val name = service.name
    val mileageChange = service.mileageChange
    val mustBeDoneBefore = service.mustBeDoneBefore
    if (name != null && mileageChange != null && mustBeDoneBefore != null) {
      ServiceOption(name, mileageChange, mustBeDoneBefore)
    } else {
      null
    }
  }

/**
 * Opcoes do menu e qual comeca selecionada.
 *
 * Antes, uma descricao fora do catalogo caia no primeiro servico, e salvar
 * trocava a descricao sem aviso. Agora ela entra como primeira opcao e comeca
 * selecionada. Descricao nula ou em branco continua caindo no primeiro servico:
 * nao ha dado a perder.
 */
internal fun serviceChoices(catalog: List<ServiceOption>, editingDescription: String?): ServiceChoices {
  if (editingDescription.isNullOrBlank()) return ServiceChoices(catalog, 0)

  val index = catalog.indexOfFirst { it.name == editingDescription }
  if (index >= 0) return ServiceChoices(catalog, index)

  val outsideCatalog = ServiceOption(editingDescription, mileageChange = null, mustBeDoneBefore = null)
  return ServiceChoices(listOf(outsideCatalog) + catalog, 0)
}

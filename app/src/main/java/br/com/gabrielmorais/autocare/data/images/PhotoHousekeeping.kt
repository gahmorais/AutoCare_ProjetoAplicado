package br.com.gabrielmorais.autocare.data.images

import br.com.gabrielmorais.autocare.data.local.repository.ProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import kotlinx.coroutines.flow.first

/**
 * Recolhe as fotos que nenhum perfil ou veiculo referencia mais. E o unico
 * caminho de exclusao de foto do app - ver [LocalImageStore].
 */
class PhotoHousekeeping(
  private val profiles: ProfileRepository,
  private val vehicles: VehicleRepository,
  private val imageStore: LocalImageStore
) {
  /** @return quantas fotos foram apagadas. */
  suspend fun sweep(): Int {
    val referenced = buildSet {
      profiles.observeProfiles().first().mapNotNullTo(this) { it.photoPath }
      vehicles.observeAllVehicles().first().mapNotNullTo(this) { it.photo }
    }
    return imageStore.sweepOrphans(referenced)
  }
}

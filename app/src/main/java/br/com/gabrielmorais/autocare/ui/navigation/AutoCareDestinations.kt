package br.com.gabrielmorais.autocare.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.gabrielmorais.autocare.R

/**
 * Os tres destinos da barra inferior. Substituem o drawer e a duplicacao entre
 * a antiga tela inicial e "Minha Conta", que listavam veiculos as duas.
 */
enum class TopLevelDestination(
  val route: String,
  @StringRes val labelRes: Int,
  val icon: ImageVector
) {
  INICIO("inicio", R.string.nav_home, Icons.Outlined.Home),
  VEICULOS("veiculos", R.string.nav_vehicles, Icons.Outlined.DirectionsCar),
  EU("eu", R.string.nav_account, Icons.Outlined.Person)
}

object Routes {
  const val VEHICLE_ID = "vehicleId"
  const val MAINTENANCE_ID = "maintenanceId"

  /** Nenhuma manutencao selecionada: o formulario abre em modo de criacao. */
  const val NO_MAINTENANCE = -1

  const val VEHICLE_DETAILS = "veiculo/{$VEHICLE_ID}"
  fun vehicleDetails(vehicleId: String) = "veiculo/$vehicleId"

  const val MAINTENANCE_FORM = "veiculo/{$VEHICLE_ID}/manutencao?$MAINTENANCE_ID={$MAINTENANCE_ID}"

  /**
   * O id da manutencao viaja como Int e nao o objeto inteiro: Navigation Compose
   * nao carrega Parcelable em argumento de rota, e o formulario ja rele o
   * veiculo de qualquer jeito.
   */
  fun maintenanceForm(vehicleId: String, maintenanceId: Int = NO_MAINTENANCE) =
    "veiculo/$vehicleId/manutencao?$MAINTENANCE_ID=$maintenanceId"
}

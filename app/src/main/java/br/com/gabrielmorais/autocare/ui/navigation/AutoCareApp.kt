package br.com.gabrielmorais.autocare.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.gabrielmorais.autocare.ui.activities.account_screen.AccountScreen
import br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen.MaintenanceFormRoute
import br.com.gabrielmorais.autocare.ui.activities.home_screen.HomeScreen
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsRoute
import br.com.gabrielmorais.autocare.ui.activities.vehicles_screen.VehiclesScreen

/**
 * O app inteiro num NavHost so. Antes eram cinco Activities ligadas por Intent,
 * sem botao de voltar em duas delas e com a lista de veiculos duplicada entre a
 * tela inicial e "Minha Conta".
 */
@Composable
fun AutoCareApp(
  shellViewModel: MainViewModel,
  onLoggedOut: () -> Unit,
  navController: NavHostController = rememberNavController()
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = backStackEntry?.destination

  // A barra some nos destinos de detalhe e de formulario: la o que vale e o
  // botao de voltar, e manter as abas convidaria a perder o contexto do veiculo.
  val showBottomBar = TopLevelDestination.values().any { destination ->
    currentDestination?.hierarchy?.any { it.route == destination.route } == true
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (showBottomBar) {
        NavigationBar {
          TopLevelDestination.values().forEach { destination ->
            val selected =
              currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
              selected = selected,
              onClick = { navController.navigateToTopLevel(destination) },
              // Icone e rotulo juntos: rotulo sozinho no destino ativo esconde
              // os demais e obriga o usuario a adivinhar.
              icon = {
                Icon(
                  imageVector = destination.icon,
                  contentDescription = null
                )
              },
              label = { Text(stringResource(destination.labelRes)) },
              // O indicador padrao do M3 usa secondaryContainer, que aqui e o
              // oliva reservado para "em dia" - aba ativa nao pode falar a
              // mesma lingua do status da manutencao.
              colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onSurface
              )
            )
          }
        }
      }
    }
  ) { padding ->
    NavHost(
      navController = navController,
      startDestination = TopLevelDestination.INICIO.route,
      modifier = Modifier.padding(padding)
    ) {
      composable(TopLevelDestination.INICIO.route) {
        HomeScreen(
          viewModel = shellViewModel,
          onOpenVehicle = { navController.navigate(Routes.vehicleDetails(it)) },
          onOpenMaintenance = { vehicleId, maintenanceId ->
            navController.navigate(Routes.maintenanceForm(vehicleId, maintenanceId))
          },
          onAddVehicle = { navController.navigateToTopLevel(TopLevelDestination.VEICULOS) }
        )
      }

      composable(TopLevelDestination.VEICULOS.route) {
        VehiclesScreen(
          viewModel = shellViewModel,
          onOpenVehicle = { navController.navigate(Routes.vehicleDetails(it)) }
        )
      }

      composable(TopLevelDestination.EU.route) {
        AccountScreen(
          viewModel = shellViewModel,
          onLoggedOut = onLoggedOut
        )
      }

      composable(
        route = Routes.VEHICLE_DETAILS,
        arguments = listOf(navArgument(Routes.VEHICLE_ID) { type = NavType.StringType })
      ) { entry ->
        VehicleDetailsRoute(
          vehicleId = entry.arguments?.getString(Routes.VEHICLE_ID).orEmpty(),
          onBack = { navController.popBackStack() },
          onAddMaintenance = { vehicleId ->
            navController.navigate(Routes.maintenanceForm(vehicleId))
          },
          onEditMaintenance = { vehicleId, maintenanceId ->
            navController.navigate(Routes.maintenanceForm(vehicleId, maintenanceId))
          }
        )
      }

      composable(
        route = Routes.MAINTENANCE_FORM,
        arguments = listOf(
          navArgument(Routes.VEHICLE_ID) { type = NavType.StringType },
          navArgument(Routes.MAINTENANCE_ID) {
            type = NavType.IntType
            defaultValue = Routes.NO_MAINTENANCE
          }
        )
      ) { entry ->
        MaintenanceFormRoute(
          vehicleId = entry.arguments?.getString(Routes.VEHICLE_ID).orEmpty(),
          maintenanceId = entry.arguments?.getInt(Routes.MAINTENANCE_ID) ?: Routes.NO_MAINTENANCE,
          onBack = { navController.popBackStack() }
        )
      }
    }
  }
}

/**
 * Troca de aba sem empilhar: volta ao inicio do grafo salvando o estado da aba
 * que sai e restaurando o da que entra. Sem isso, alternar abas empilharia
 * destinos e o botao voltar percorreria todo o historico de cliques.
 */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
  navigate(destination.route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
  }
}

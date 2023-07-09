package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen.AddMaintenanceViewModel
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainViewModel
import br.com.gabrielmorais.autocare.ui.activities.my_account_screen.MyAccountViewModel
import br.com.gabrielmorais.autocare.ui.activities.register_screen.RegisterViewModel
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { VehicleDetailsViewModel(get()) }
  viewModel { MainViewModel(get(), get()) }
  viewModel { RegisterViewModel(get(), get()) }
  viewModel { MyAccountViewModel(get()) }
  viewModel { AddMaintenanceViewModel(get(), get(), get()) }
}
package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { VehicleDetailsViewModel(get()) }
}
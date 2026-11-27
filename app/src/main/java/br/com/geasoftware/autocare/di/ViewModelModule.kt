package br.com.geasoftware.autocare.di

import br.com.geasoftware.autocare.ui.activities.add_maintenance_screen.AddMaintenanceViewModel
import br.com.geasoftware.autocare.ui.activities.main_screen.MainViewModel
import br.com.geasoftware.autocare.ui.activities.vehicle_details_screen.VehicleDetailsViewModel
import br.com.geasoftware.autocare.ui.activities.profile_screen.BackupViewModel
import br.com.geasoftware.autocare.ui.activities.profile_screen.ContentResolverDocumentStreams
import br.com.geasoftware.autocare.ui.activities.profile_screen.NotificationImportSideEffects
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
  viewModel { VehicleDetailsViewModel(get(), get(), get(), get()) }
  viewModel { MainViewModel(get(), get(), get(), get()) }
  viewModel { AddMaintenanceViewModel(get(), get(), get()) }
  viewModel {
    BackupViewModel(
      exporter = get(),
      importer = get(),
      streams = ContentResolverDocumentStreams(androidContext()),
      sideEffects = NotificationImportSideEffects(androidContext())
    )
  }
}
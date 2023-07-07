package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import org.koin.dsl.module

val mainModule = module {
  single { VehicleRepositoryImpl(get(), get()) }
  single { AuthRepositoryImpl(get()) }
  single { UserRepositoryImpl(get(), get()) }
  single { MaintenanceRepositoryImpl(get()) }
  single { ServicesRepositoryImpl(get()) }
}
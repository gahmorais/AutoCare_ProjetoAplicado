package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepository
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryImpl
import org.koin.dsl.module

// As ViewModels dependem das interfaces, nao das implementacoes, para permitir
// substituicao por fakes nos testes.
val mainModule = module {
  single<VehicleRepository> { VehicleRepositoryImpl(get(), get()) }
  single<AuthRepository> { AuthRepositoryImpl(get()) }
  single<UserRepository> { UserRepositoryImpl(get(), get()) }
  single<MaintenanceRepository> { MaintenanceRepositoryImpl(get()) }
  single<ServicesRepository> { ServicesRepositoryImpl(get()) }
}

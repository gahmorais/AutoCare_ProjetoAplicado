package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.repository.AssetServicesRepository
import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.ProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomVehicleRepository
import br.com.gabrielmorais.autocare.data.local.repository.ServicesRepository
import br.com.gabrielmorais.autocare.data.local.repository.VehicleRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Armazenamento local. Registrado ja na etapa 2 mas sem consumidor ate a etapa 4:
 * os singles do Koin sao preguicosos, entao o banco so e aberto quando alguem
 * pedir um destes repositorios. As interfaces sao de outro pacote que as do
 * Firebase, entao nao ha conflito de tipo no grafo.
 */
val localDataModule = module {
  single { AutoCareDatabase.build(androidContext()) }
  single<ProfileRepository> { RoomProfileRepository(get()) }
  single<VehicleRepository> { RoomVehicleRepository(get()) }
  single<MaintenanceRepository> { RoomMaintenanceRepository(get()) }
  single<ServicesRepository> { AssetServicesRepository(androidContext().assets) }
}

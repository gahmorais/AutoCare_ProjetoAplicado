package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.data.images.ImageCompressor
import br.com.gabrielmorais.autocare.data.images.LocalImageStore
import br.com.gabrielmorais.autocare.data.images.PhotoHousekeeping
import br.com.gabrielmorais.autocare.data.local.ActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.DataStoreActiveProfileStore
import br.com.gabrielmorais.autocare.data.local.settingsDataStore
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
 * Armazenamento local: banco, fotos e perfil ativo. E todo o grafo de dados do
 * app - nao ha servidor.
 */
val localDataModule = module {
  single { AutoCareDatabase.build(androidContext()) }
  single<ProfileRepository> { RoomProfileRepository(get()) }
  single<VehicleRepository> { RoomVehicleRepository(get()) }
  single<MaintenanceRepository> { RoomMaintenanceRepository(get()) }
  single<ServicesRepository> { AssetServicesRepository(androidContext().assets) }
  single<ActiveProfileStore> { DataStoreActiveProfileStore(androidContext().settingsDataStore) }
  single { PhotoHousekeeping(get(), get(), get()) }
  single {
    val context = androidContext()
    LocalImageStore(
      rootDir = context.filesDir,
      cacheDir = context.cacheDir,
      compress = { source, kind -> ImageCompressor(kind).compress(context.contentResolver, source) }
    )
  }
}

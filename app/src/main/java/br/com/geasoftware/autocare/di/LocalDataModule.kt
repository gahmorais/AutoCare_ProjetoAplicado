package br.com.geasoftware.autocare.di

import br.com.geasoftware.autocare.BuildConfig
import br.com.geasoftware.autocare.data.backup.BackupExporter
import br.com.geasoftware.autocare.data.backup.BackupImporter
import br.com.geasoftware.autocare.data.backup.BackupStore
import br.com.geasoftware.autocare.data.backup.BitmapImportedImageCheck
import br.com.geasoftware.autocare.data.backup.RoomBackupStore
import br.com.geasoftware.autocare.data.images.ImageCompressor
import br.com.geasoftware.autocare.data.images.LocalImageStore
import br.com.geasoftware.autocare.data.images.PhotoHousekeeping
import br.com.geasoftware.autocare.data.local.ActiveProfileStore
import br.com.geasoftware.autocare.data.local.DataStoreActiveProfileStore
import br.com.geasoftware.autocare.data.local.settingsDataStore
import br.com.geasoftware.autocare.data.local.AutoCareDatabase
import br.com.geasoftware.autocare.data.local.repository.AssetServicesRepository
import br.com.geasoftware.autocare.data.local.repository.MaintenanceRepository
import br.com.geasoftware.autocare.data.local.repository.ProfileRepository
import br.com.geasoftware.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.geasoftware.autocare.data.local.repository.RoomProfileRepository
import br.com.geasoftware.autocare.data.local.repository.RoomVehicleRepository
import br.com.geasoftware.autocare.data.local.repository.ServicesRepository
import br.com.geasoftware.autocare.data.local.repository.VehicleRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File

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
  single<BackupStore> { RoomBackupStore(get()) }
  single {
    BackupExporter(
      store = get(),
      imagesRoot = androidContext().filesDir,
      appVersion = BuildConfig.VERSION_NAME
    )
  }
  single {
    val context = androidContext()
    BackupImporter(
      store = get(),
      imagesRoot = context.filesDir,
      stagingRoot = File(context.cacheDir, "backup-import"),
      imageCheck = BitmapImportedImageCheck(context.contentResolver)
    )
  }
  single {
    val context = androidContext()
    LocalImageStore(
      rootDir = context.filesDir,
      cacheDir = context.cacheDir,
      compress = { source, kind -> ImageCompressor(kind).compress(context.contentResolver, source) }
    )
  }
}

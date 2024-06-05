package br.com.gabrielmorais.autocare.di

import androidx.room.Room
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.repositories.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repositories.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repositories.user.UserRepository
import br.com.gabrielmorais.autocare.data.repositories.vehicleRepository.VehicleRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.ResourceProvider
import org.koin.dsl.module

val mainModule = module {
  single {
    Room.databaseBuilder(
      get(),
      AppDatabase::class.java,
      "database"
    ).build()
  }
  single { ResourceProvider(get()) }
  single { AuthRepository(get(),get<AppDatabase>().userDao()) }
  single { UserRepository(get<AppDatabase>().userDao()) }
  single { ImageUtils(get()) }
  single { MaintenanceRepository(get<AppDatabase>().maintenanceDao()) }
  single { VehicleRepository(get<AppDatabase>().vehicleDao()) }
}
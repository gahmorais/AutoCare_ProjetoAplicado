package br.com.gabrielmorais.autocare.di

import androidx.room.Room
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.repositories.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repositories.authorization.IAuthRepository
import br.com.gabrielmorais.autocare.data.repositories.maintenance.IMaintenanceRepository
import br.com.gabrielmorais.autocare.data.repositories.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repositories.user.IUserRepository
import br.com.gabrielmorais.autocare.data.repositories.user.UserRepository
import br.com.gabrielmorais.autocare.data.repositories.vehicleRepository.IVehicleRepository
import br.com.gabrielmorais.autocare.data.repositories.vehicleRepository.VehicleRepository
import br.com.gabrielmorais.autocare.utils.ImageUtils
import br.com.gabrielmorais.autocare.utils.ResourceProvider
import org.koin.dsl.module

val mainModule = module {

  single<IAuthRepository> { AuthRepository(get(), get<AppDatabase>().userDao()) }
  single<IUserRepository> { UserRepository(get<AppDatabase>().userDao()) }
  single<IMaintenanceRepository> { MaintenanceRepository(get<AppDatabase>().maintenanceDao()) }
  single<IVehicleRepository> { VehicleRepository(get<AppDatabase>().vehicleDao()) }
  single {
    Room.databaseBuilder(get(), AppDatabase::class.java, "database").build()
  }
  single { ResourceProvider(get()) }
  single { ImageUtils(get()) }
}
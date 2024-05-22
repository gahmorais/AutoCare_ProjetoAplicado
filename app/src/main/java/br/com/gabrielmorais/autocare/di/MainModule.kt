package br.com.gabrielmorais.autocare.di

import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryFirebase
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryFirebase
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepositoryFirebase
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.user.UserRepository
import br.com.gabrielmorais.autocare.data.repository.user.UserRepositoryFirebase
import br.com.gabrielmorais.autocare.utils.ImageUtils
import org.koin.dsl.module

val mainModule = module {
  single {
    Room.databaseBuilder(
      get(),
      AppDatabase::class.java,
      "database"
    ).build()
  }
  single { VehicleRepositoryFirebase(get(), get()) }
  single { AuthRepositoryFirebase(get()) }
  single { UserRepositoryFirebase(get(), get()) }
  single { MaintenanceRepositoryFirebase(get()) }
  single { ServicesRepositoryImpl(get()) }

  single { AuthRepository(get(), get()) }
  single { UserRepository(get()) }
  single { ImageUtils(get()) }
}
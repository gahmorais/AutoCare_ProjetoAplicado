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
import android.content.Context
import br.com.gabrielmorais.autocare.BuildConfig
import br.com.gabrielmorais.autocare.data.images.CloudinaryImageUploader
import br.com.gabrielmorais.autocare.data.images.CloudinaryUploadApi
import br.com.gabrielmorais.autocare.data.images.ImageUploader
import okhttp3.OkHttpClient
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

// As ViewModels dependem das interfaces, nao das implementacoes, para permitir
// substituicao por fakes nos testes.
val mainModule = module {
  single {
    OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(60, TimeUnit.SECONDS)
      .writeTimeout(60, TimeUnit.SECONDS)
      .build()
  }

  single {
    CloudinaryUploadApi(
      client = get(),
      cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
      uploadPreset = BuildConfig.CLOUDINARY_UPLOAD_PRESET
    )
  }

  single<ImageUploader> {
    CloudinaryImageUploader(
      contentResolver = get<Context>().contentResolver,
      api = get()
    )
  }

  single<VehicleRepository> { VehicleRepositoryImpl(get(), get()) }
  single<AuthRepository> { AuthRepositoryImpl(get()) }
  single<UserRepository> { UserRepositoryImpl(get(), get()) }
  single<MaintenanceRepository> { MaintenanceRepositoryImpl(get()) }
  single<ServicesRepository> { ServicesRepositoryImpl(get()) }
}

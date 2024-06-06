package br.com.gabrielmorais.autocare

import android.app.Application
import br.com.gabrielmorais.autocare.di.mainModule
import br.com.gabrielmorais.autocare.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class AutoCareApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@AutoCareApplication)
      modules(mainModule, viewModelModule)
    }
  }
}
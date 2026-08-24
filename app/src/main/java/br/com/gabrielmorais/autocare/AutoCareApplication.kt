package br.com.gabrielmorais.autocare

import android.app.Application
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.di.firebaseModule
import br.com.gabrielmorais.autocare.di.mainModule
import br.com.gabrielmorais.autocare.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AutoCareApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@AutoCareApplication)
      modules(listOf(mainModule, firebaseModule, viewModelModule))
    }

    // O canal precisa existir antes de qualquer notify, inclusive quando o
    // processo e iniciado pelo alarme e nenhuma Activity chegou a rodar.
    NotificationUtils.createNotificationChannel(this)
  }
}
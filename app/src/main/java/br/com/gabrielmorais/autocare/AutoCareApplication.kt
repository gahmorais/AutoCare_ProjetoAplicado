package br.com.gabrielmorais.autocare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
    val channel = NotificationChannel(
      "channel_id",
      "channel_name",
      NotificationManager.IMPORTANCE_HIGH
    )

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)

    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@AutoCareApplication)
      modules(listOf(mainModule, firebaseModule, viewModelModule))
    }
  }
}
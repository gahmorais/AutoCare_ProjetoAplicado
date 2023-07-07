package br.com.gabrielmorais.autocare

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import br.com.gabrielmorais.autocare.di.firebaseModule
import br.com.gabrielmorais.autocare.di.mainModule
import br.com.gabrielmorais.autocare.di.viewModelModule
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AutoCareApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    val CHANNEL_ID = "channelId"
    val CHANNEL_NAME = "channelName"
    val NOTIFICATION_ID = 0
    val channel = NotificationChannel(
      CHANNEL_ID,
      CHANNEL_NAME,
      NotificationManager.IMPORTANCE_HIGH
    )

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)

    startKoin {
      androidLogger(Level.DEBUG)
      androidContext(this@AutoCareApplication)
      modules(listOf(mainModule, firebaseModule, viewModelModule))
    }



    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.d("AutoCareApplication", "onCreate: FAlha ao recuperar o token")
        return@addOnCompleteListener
      }
      val token = task.result
      val msg = "Token $token"

    }
  }
}
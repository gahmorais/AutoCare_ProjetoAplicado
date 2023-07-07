package br.com.gabrielmorais.autocare.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import br.com.gabrielmorais.autocare.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessageService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    super.onNewToken(token)
  }

  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
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
    Log.d("FirebaseMessageService", "onMessageReceived: ${message.from}")
    if (message.data.isNotEmpty()) {
      Log.d("FirebaseMessageService", "onMessageReceived: Message content: ${message.data}")
    }
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
    message.notification?.let {
      val updatedNotify = notification
        .setContentTitle(it.title)
        .setSmallIcon(R.drawable.car_repair_placeholder)
        .setContentText(it.body)
        .build()

      notificationManager.notify(NOTIFICATION_ID, updatedNotify)

    }
  }
}
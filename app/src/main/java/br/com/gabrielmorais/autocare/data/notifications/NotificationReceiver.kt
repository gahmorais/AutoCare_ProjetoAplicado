package br.com.gabrielmorais.autocare.data.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance

const val channelID = "channel1"

@Suppress("DEPRECATION")
class NotificationReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {

    val extras = intent.extras
    val maintenanceData =
      extras?.getParcelable<Maintenance>(context.getString(R.string.MAINTENANCE_INTENT))

    val notification = NotificationCompat.Builder(context, channelID)
      .setSmallIcon(R.mipmap.ic_autocare_logo)
      .setContentTitle(context.getString(R.string.maintenance_coming))
      .setContentText(maintenanceData?.description)
      .build()

    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(maintenanceData!!.id, notification)
  }
}
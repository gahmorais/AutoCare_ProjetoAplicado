package br.com.gabrielmorais.autocare.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.ui.activities.login_screen.LoginActivity

const val channelID = "channel1"

class NotificationReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val maintenanceData = intent.getParcelableExtraCompat<Maintenance>(
      context.getString(R.string.MAINTENANCE_INTENT)
    )

    // O !! aqui derrubava o processo se o extra viesse ausente.
    if (maintenanceData == null) {
      Log.w("NotificationReceiver", "onReceive: broadcast sem dados da manutenção")
      return
    }

    if (!hasNotificationPermission(context)) {
      Log.w("NotificationReceiver", "onReceive: sem permissão de notificação")
      return
    }

    // LoginActivity redireciona para MainActivity quando ja ha sessao ativa.
    val contentIntent = Intent(context, LoginActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      maintenanceData.id,
      contentIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, channelID)
      .setSmallIcon(R.mipmap.ic_autocare_logo)
      .setContentTitle(context.getString(R.string.maintenance_coming))
      .setContentText(maintenanceData.description)
      .setContentIntent(contentPendingIntent)
      .setAutoCancel(true)
      .build()

    androidx.core.app.NotificationManagerCompat.from(context)
      .notify(maintenanceData.id, notification)
  }

  private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
}

@Suppress("DEPRECATION")
private inline fun <reified T : android.os.Parcelable> Intent.getParcelableExtraCompat(
  key: String
): T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
  getParcelableExtra(key, T::class.java)
} else {
  getParcelableExtra(key)
}

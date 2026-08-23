package br.com.gabrielmorais.autocare.data.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationUtils {
  fun createNotificationChannel(context: Context) {
    val name = "Autocare Channel"
    val desc = "Notification Channel for Autocare App"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelID, name, importance)
    channel.description = desc
    val notificationManager = context
      .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  /**
   * A versao anterior montava um [java.util.Calendar] com `Calendar.set(year, month, ...)`
   * usando o mes 1-12 do [LocalDateTime], mas Calendar espera 0-11: todo agendamento
   * caia um mes adiante.
   */
  private fun toEpochMillis(localDateTime: LocalDateTime): Long =
    localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

  fun scheduleNotification(
    context: Context,
    localDateTime: LocalDateTime,
    maintenance: Maintenance
  ) {
    val time = toEpochMillis(localDateTime)
    if (time <= System.currentTimeMillis()) {
      // Alarme no passado dispara imediatamente; nao ha o que notificar.
      Log.i("NotificationUtils", "scheduleNotification: data ja passou ($localDateTime)")
      return
    }

    val intent = Intent(context.applicationContext, NotificationReceiver::class.java)
    intent.putExtra(context.getString(R.string.MAINTENANCE_INTENT), maintenance)

    val pendingIntent = PendingIntent.getBroadcast(
      context.applicationContext,
      maintenance.id,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Alarme inexato de proposito: com targetSdk 34 o SCHEDULE_EXACT_ALARM deixou de
    // ser concedido automaticamente e setExactAndAllowWhileIdle lancava SecurityException.
    // Para um lembrete com cinco dias de antecedencia a precisao do Doze e suficiente.
    alarmManager.setAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      time,
      pendingIntent
    )
    Log.i("NotificationUtils", "scheduleNotification: agendada para $localDateTime")
  }

  fun cancelNotification(context: Context, maintenance: Maintenance) {
    val intent = Intent(context.applicationContext, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
      context.applicationContext,
      maintenance.id,
      intent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
  }
}

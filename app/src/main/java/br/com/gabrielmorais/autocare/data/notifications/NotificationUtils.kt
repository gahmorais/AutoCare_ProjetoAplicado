package br.com.gabrielmorais.autocare.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import timber.log.Timber
import java.time.LocalDateTime
import java.util.Calendar

object NotificationUtils {
  fun createNotificationChannel(context: Context) {
    val name = "Autocare Channel"
    val desc = "Notification Channel for Autocare App"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel = NotificationChannel(channelID, name, importance)
    channel.description = desc
    val notificationManager = context
      .getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
  }

  private fun getTime(localDateTime: LocalDateTime): Long {
    val minute = localDateTime.minute
    val hour = localDateTime.hour
    val day = localDateTime.dayOfMonth
    val month = localDateTime.month.value - 1
    val year = localDateTime.year

    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, hour, minute)
    return calendar.timeInMillis
  }

  fun scheduleNotification(
    context: Context,
    localDateTime: LocalDateTime,
    maintenance: Maintenance
  ) {
    val intent = Intent(context.applicationContext, NotificationReceiver::class.java)
    intent.putExtra(context.getString(R.string.MAINTENANCE_INTENT), maintenance)
    Timber.tag("NotificationUtils").i("scheduleNotification: Notificação agendada $localDateTime")
    Timber.tag("NotificationUtils")
      .i("scheduleNotification: Identificação notificação ${maintenance.id}")

//    val pendingIntent = PendingIntent.getBroadcast(
//      context.applicationContext,
//      maintenance.id,
//      intent,
//      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//    )
//
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    val time = getTime(localDateTime)
//    alarmManager.setExactAndAllowWhileIdle(
//      AlarmManager.RTC_WAKEUP,
//      time,
//      pendingIntent
//    )
  }
}
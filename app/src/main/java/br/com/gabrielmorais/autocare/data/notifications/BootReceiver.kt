package br.com.gabrielmorais.autocare.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    enqueueReschedule(context)
  }

  companion object {
    /**
     * A leitura do Firebase nao cabe nos ~10s de um BroadcastReceiver, entao o
     * trabalho vai para o WorkManager, que tambem cuida do retry sem rede.
     */
    fun enqueueReschedule(context: Context) {
      val request = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>()
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .build()

      WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
        RescheduleNotificationsWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request
      )
    }
  }
}

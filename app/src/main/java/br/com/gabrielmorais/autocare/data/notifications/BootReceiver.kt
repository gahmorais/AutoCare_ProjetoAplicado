package br.com.gabrielmorais.autocare.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    enqueueReschedule(context)
  }

  companion object {
    /**
     * Banco e disco nao rodam na main thread de um BroadcastReceiver, entao o
     * trabalho vai para o WorkManager. Sem restricao de rede: os dados sao
     * locais, e esperar conexao atrasaria os lembretes de quem esta offline.
     */
    fun enqueueReschedule(context: Context) {
      val request = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>().build()

      WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
        RescheduleNotificationsWorker.WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request
      )
    }
  }
}

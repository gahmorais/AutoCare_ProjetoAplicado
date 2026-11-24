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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.ui.activities.main_screen.MainActivity
import br.com.gabrielmorais.autocare.utils.getParcelableExtraCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val channelID = "channel1"

/**
 * Dispara o lembrete agendado por [NotificationUtils]. O alarme carrega so o id
 * (dentro da [Maintenance] do extra); veiculo, dono e a propria manutencao sao
 * relidos do banco agora, para o texto refletir renomeacao, transferencia de
 * perfil ou exclusao feitas depois do agendamento.
 */
class NotificationReceiver : BroadcastReceiver(), KoinComponent {

  private val maintenanceRepository: MaintenanceRepository by inject()

  override fun onReceive(context: Context, intent: Intent) {
    val scheduled = intent.getParcelableExtraCompat<Maintenance>(
      context.getString(R.string.MAINTENANCE_INTENT)
    )
    // O !! aqui derrubava o processo se o extra viesse ausente.
    if (scheduled == null) {
      Log.w(TAG, "onReceive: broadcast sem dados da manutenção")
      return
    }

    // Banco nao roda na main thread do receiver: goAsync mantem o broadcast
    // vivo ate a leitura terminar (limite de ~10 s, folgado para uma consulta).
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
      try {
        val reminder = runCatching { maintenanceRepository.getReminder(scheduled.id) }
          .onFailure { Log.w(TAG, "onReceive: falha ao ler o lembrete", it) }
          .getOrNull()
        when {
          // Lida com sucesso e ausente: a manutencao foi excluida, nao ha o que avisar.
          reminder == null && maintenanceExists(scheduled.id) == false -> return@launch
          reminder?.maintenance?.completed == true -> return@launch
        }
        val text = reminder?.let { ReminderText.of(it) } ?: scheduled.description.orEmpty()
        notify(context, scheduled.id, text)
      } finally {
        pending.finish()
      }
    }
  }

  /** null quando nem a leitura deu certo: na duvida, avisa com o texto do agendamento. */
  private suspend fun maintenanceExists(id: Int): Boolean? =
    runCatching { maintenanceRepository.getMaintenance(id) != null }.getOrNull()

  private fun notify(context: Context, notificationId: Int, text: String) {
    // Check inline (e nao extraido para um helper) para que o lint consiga
    // rastrear que a permissao foi verificada antes do notify.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      Log.w(TAG, "notify: sem permissão de notificação")
      return
    }

    val contentIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      notificationId,
      contentIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, channelID)
      .setSmallIcon(R.mipmap.ic_autocare_logo)
      .setContentTitle(context.getString(R.string.maintenance_coming))
      .setContentText(text)
      // O texto com veiculo e dono passa de uma linha; sem o BigText ele e cortado.
      .setStyle(NotificationCompat.BigTextStyle().bigText(text))
      .setContentIntent(contentPendingIntent)
      .setAutoCancel(true)
      .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
  }

  private companion object {
    const val TAG = "NotificationReceiver"
  }
}

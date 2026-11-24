package br.com.gabrielmorais.autocare.data.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.gabrielmorais.autocare.data.images.PhotoHousekeeping
import br.com.gabrielmorais.autocare.data.local.repository.MaintenanceRepository
import br.com.gabrielmorais.autocare.utils.Utils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

/**
 * O AlarmManager perde todos os alarmes quando o aparelho reinicia, e nada os
 * recriava: as notificacoes agendadas simplesmente nunca chegavam. Este worker
 * rele as manutencoes pendentes de todos os perfis e reagenda as que ainda
 * estao no futuro.
 *
 * Aproveita a mesma passada para recolher fotos sem dono ([PhotoHousekeeping]):
 * roda no boot e a cada abertura do app, fora da thread principal.
 */
class RescheduleNotificationsWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

  private val maintenanceRepository: MaintenanceRepository by inject()
  private val photoHousekeeping: PhotoHousekeeping by inject()

  override suspend fun doWork(): Result {
    val pending = runCatching { maintenanceRepository.getPendingWithForecast() }
      .getOrElse { error ->
        Log.w(TAG, "doWork: falha ao ler manutenções", error)
        return Result.retry()
      }

    val today = LocalDate.now()
    var scheduled = 0
    pending.forEach { maintenance ->
      val forecastDate = maintenance.forecastNextExchangeDate?.let(LocalDate::ofEpochDay)
        ?: return@forEach
      if (forecastDate.isAfter(today)) {
        NotificationUtils.scheduleNotification(
          context = applicationContext,
          localDateTime = Utils.dateMinusFiveDays(forecastDate),
          maintenance = maintenance
        )
        scheduled++
      }
    }
    Log.i(TAG, "doWork: $scheduled notificações reagendadas")

    // Falha na limpeza nao invalida o reagendamento: tenta de novo na proxima.
    runCatching { photoHousekeeping.sweep() }
      .onSuccess { Log.i(TAG, "doWork: $it fotos sem dono removidas") }
      .onFailure { Log.w(TAG, "doWork: falha na limpeza de fotos", it) }

    return Result.success()
  }

  companion object {
    const val TAG = "RescheduleNotifications"
    const val WORK_NAME = "reschedule-maintenance-notifications"
  }
}

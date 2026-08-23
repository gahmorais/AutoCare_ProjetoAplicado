package br.com.gabrielmorais.autocare.data.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import br.com.gabrielmorais.autocare.utils.Utils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

/**
 * O AlarmManager perde todos os alarmes quando o aparelho reinicia, e nada os
 * recriava: as notificacoes agendadas simplesmente nunca chegavam. Este worker
 * relê as manutencoes do usuario logado e reagenda as que ainda estao no futuro.
 */
class RescheduleNotificationsWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

  private val authRepository: AuthRepository by inject()
  private val vehicleRepository: VehicleRepository by inject()

  override suspend fun doWork(): Result {
    val userId = authRepository.getCurrentUser()?.uid
    if (userId == null) {
      Log.i(TAG, "doWork: nenhum usuário logado, nada a reagendar")
      return Result.success()
    }

    val vehicles = runCatching { vehicleRepository.getVehiclesOnce(userId) }
      .getOrElse { error ->
        Log.w(TAG, "doWork: falha ao ler veículos", error)
        return Result.retry()
      }

    val today = LocalDate.now()
    var scheduled = 0

    vehicles.forEach { vehicle ->
      vehicle.maintenances.orEmpty().forEach maintenances@{ maintenance ->
        val forecastEpochDay = maintenance.forecastNextExchangeDate ?: return@maintenances
        val forecastDate = LocalDate.ofEpochDay(forecastEpochDay)
        if (forecastDate.isAfter(today)) {
          NotificationUtils.scheduleNotification(
            context = applicationContext,
            localDateTime = Utils.dateMinusFiveDays(forecastDate),
            maintenance = maintenance
          )
          scheduled++
        }
      }
    }

    Log.i(TAG, "doWork: $scheduled notificações reagendadas")
    return Result.success()
  }

  companion object {
    const val TAG = "RescheduleNotifications"
    const val WORK_NAME = "reschedule-maintenance-notifications"
  }
}

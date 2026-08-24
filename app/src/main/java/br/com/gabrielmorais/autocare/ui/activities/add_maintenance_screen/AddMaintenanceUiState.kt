package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.gabrielmorais.autocare.data.models.Maintenance
import java.time.LocalDate

/**
 * [maintenance] semeia os campos quando a tela abre para editar um registro
 * existente; em modo de criacao vem null e valem os defaults. Campos nulos de
 * registros antigos tambem caem nos defaults em vez de derrubar a tela.
 */
class AddMaintenanceUiState(maintenance: Maintenance? = null) {
  var date: LocalDate by mutableStateOf(maintenance?.date.toLocalDateOrToday())
    private set
  var currentMileage: String by mutableStateOf(maintenance?.currentMileage?.toString() ?: "0")
    private set
  var forecastNextExchangeMileage: String by mutableStateOf(
    maintenance?.forecastNextExchangeMileage?.toString() ?: "0"
  )
    private set
  var forecastNextExchangeDate: LocalDate by mutableStateOf(
    maintenance?.forecastNextExchangeDate.toLocalDateOrToday()
  )
    private set
  var comments: String by mutableStateOf(maintenance?.comments.orEmpty())
    private set
  var completed: Boolean by mutableStateOf(maintenance?.completed ?: false)
    private set

  val onDateChange: (LocalDate) -> Unit = {
    date = it
  }

  val onCurrentMilageChange: (String) -> Unit = {
    currentMileage = it
  }

  val onForecastNextExchangeMileageChange: (String) -> Unit = {
    forecastNextExchangeMileage = it
  }

  val onForecastNextExchangeDateChange: (LocalDate) -> Unit = {
    forecastNextExchangeDate = it
  }

  val onCommentsChange: (String) -> Unit = {
    comments = it
  }

  val onCompletedChange: (Boolean) -> Unit = {
    completed = it
  }
}

private fun Long?.toLocalDateOrToday(): LocalDate =
  this?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.now()

package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDate

class AddMaintenanceUiState {
  var description by mutableStateOf("")
    private set
  var date: LocalDate by mutableStateOf(LocalDate.now())
    private set
  var currentMileage: Int by mutableStateOf(0)
    private set
  var forecastNextExchangeMileage: Int by mutableStateOf(0)
    private set
  var forecastNextExchangeDate: LocalDate by mutableStateOf(LocalDate.now())
    private set
  var comments: String by mutableStateOf("")
    private set

  val onDescriptionChange: (String) -> Unit = {
    description = it
  }

  val onDateChange: (LocalDate) -> Unit = {
    date = it
  }

  val onCurrentMilageChange: (Int) -> Unit = {
    currentMileage = it
  }

  val onForecastNextExchangeMileageChange: (Int) -> Unit = {
    forecastNextExchangeMileage = it
  }

  val onForecastNextExchangeDateChange: (LocalDate) -> Unit = {
    forecastNextExchangeDate = it
  }

  val onCommentsChange: (String) -> Unit = {
    comments = it
  }

}
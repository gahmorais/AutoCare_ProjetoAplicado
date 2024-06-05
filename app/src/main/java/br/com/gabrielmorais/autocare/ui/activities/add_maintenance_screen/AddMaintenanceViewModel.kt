package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import androidx.lifecycle.ViewModel
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.repositories.maintenance.MaintenanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AddMaintenanceViewModel(
  private val maintenanRepository: MaintenanceRepository
) : ViewModel() {
  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  suspend fun saveMaintenance(maintenance: Maintenance) = try {
    maintenanRepository.create(maintenance)
  } catch (e: Exception) {
    e.printStackTrace()
    _message.update { e.message }
  }
}
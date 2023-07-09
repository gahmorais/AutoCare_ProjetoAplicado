package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepositoryImpl
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddMaintenanceViewModel(
  private val servicesRepository: ServicesRepositoryImpl,
  private val maintenanceRepository: MaintenanceRepositoryImpl,
  private val vehicleRepository: VehicleRepositoryImpl
) :
  ViewModel() {
  private val _services = MutableStateFlow<List<Service?>>(listOf())
  val services = _services.asStateFlow()

  private val _userId = MutableStateFlow("")
  val userId = _userId.asStateFlow()

  private val _vehicle = MutableStateFlow<Vehicle?>(null)
  val vehicle = _vehicle.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message = _message.asStateFlow()

  init {
    getServices()
  }

  private fun getServices() {
    servicesRepository.getServices(
      onSuccess = {
        viewModelScope.launch(Dispatchers.IO) { _services.emit(it) }
      },
      onError = {
        Log.i("AddMaintenanceViewModel", "getServices: ${it.message}")
      }
    )
  }

  fun saveMaintenance(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle
  ) {
    maintenanceRepository.create(
      userId,
      vehicleId,
      updatedVehicle,
      onSuccess = {
        viewModelScope.launch { _message.emit(it) }
      },
      onError = {
        viewModelScope.launch { _message.emit(it.message) }
      }
    )
  }

  fun setUserId(userId: String) {
    _userId.value = userId
  }

  fun getVehicle(userId: String, vehicleId: String) {
    vehicleRepository.getVehicleDetails(
      userId,
      vehicleId,
      onSuccess = {
        _vehicle.value = it
      },
      onError = {
        Log.i("AddMaintenanceViewModel", "getVehicle: ${it.message}")
      })
  }
}
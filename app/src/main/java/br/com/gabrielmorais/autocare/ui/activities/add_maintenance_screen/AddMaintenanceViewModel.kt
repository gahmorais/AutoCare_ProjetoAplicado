package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddMaintenanceViewModel(private val servicesRepository: ServicesRepositoryImpl) :
  ViewModel() {
  private val _services = MutableStateFlow<List<Service?>>(listOf())
  val services = _services.asStateFlow()

  init {
    getServices()
  }

  fun getServices() {
    servicesRepository.getServices(
      onSuccess = {
        viewModelScope.launch(Dispatchers.IO) { _services.emit(it) }
      },
      onError = {
        Log.i("AddMaintenanceViewModel", "getServices: ${it.message}")
      }
    )
  }
}
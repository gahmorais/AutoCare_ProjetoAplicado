package br.com.gabrielmorais.autocare.fakes

import android.net.Uri
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.repository.Resource
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import br.com.gabrielmorais.autocare.data.repository.maintenance.MaintenanceRepository
import br.com.gabrielmorais.autocare.data.repository.services.ServicesRepository
import br.com.gabrielmorais.autocare.data.repository.vehicleRepository.VehicleRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Fakes minimos para compor a tela de manutencao sem Firebase. Existem no
 * androidTest porque a regressao que eles guardam - retorno antecipado dentro de
 * lambda de composable derrubando o Composer - so aparece compondo de verdade.
 */
class FakeServicesRepository(
  private val services: List<Service?>? = emptyList(),
  private val error: Throwable? = null
) : ServicesRepository {
  override fun getServices(onSuccess: (List<Service?>) -> Unit, onError: (Throwable) -> Unit) {
    when {
      error != null -> onError(error)
      // null mantem a tela no estado de carregamento, que e onde o crash morava.
      services != null -> onSuccess(services)
    }
  }
}

class FakeVehicleRepository(private val vehicle: Vehicle? = null) : VehicleRepository {
  override suspend fun saveVehicleImage(image: Uri): String = ""

  override suspend fun getVehiclesOnce(userId: String): List<Vehicle> = listOfNotNull(vehicle)

  override fun getVehicleDetails(
    userId: String,
    vehicleId: String,
    onSuccess: (Vehicle) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    vehicle?.let(onSuccess)
  }

  override fun updateVehicle(
    userId: String,
    vehicleId: String,
    vehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = onSuccess("ok")
}

class FakeMaintenanceRepository : MaintenanceRepository {
  override fun create(
    userId: String,
    vehicleId: String,
    updatedVehicle: Vehicle,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = onSuccess("ok")

  override fun update(
    userId: String,
    vehicleId: String,
    maintenance: br.com.gabrielmorais.autocare.data.models.Maintenance,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = onSuccess("ok")

  override fun restore(
    userId: String,
    vehicleId: String,
    maintenance: br.com.gabrielmorais.autocare.data.models.Maintenance,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = onSuccess("ok")

  override fun delete(
    userId: String,
    vehicleId: String,
    maintenanceId: Int,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = onSuccess("ok")
}

class FakeSessionRepository(private val userId: String? = "uid-teste") : AuthRepository {
  override fun getCurrentUser(): FirebaseUser? = null
  override fun currentUserId(): String? = userId
  override fun login(email: String, password: String): Flow<Resource<AuthResult?>> = emptyFlow()
  override fun register(email: String, password: String): Flow<Resource<AuthResult>> = emptyFlow()
  override fun logout() = Unit
  override fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit) = Unit
  override fun changePassword(
    email: String,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) = Unit
}

package br.com.gabrielmorais.autocare.data.repository.user

import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle

interface UserRepository {
  fun createUser(user: User, callback: () -> Unit)
  fun getUser(userId: String, callback: (User?) -> Unit)
  fun updateUser(user: User, callback: (String) -> Unit)
  fun getVehicles(userId: String, callback: (List<Vehicle>) -> Unit)
  fun saveVehicle(userId: String, vehicle: Vehicle, callback: (String) -> Unit)
  fun deleteVehicle(userId: String, vehicleId: String, callback: (String) -> Unit)

}
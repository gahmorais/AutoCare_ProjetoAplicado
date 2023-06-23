package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import com.google.android.gms.tasks.Task

interface UserRepository {
  fun createUser(user: User, callback: (task: Task<Void>) -> Unit)
  fun getUser(userId: String, callback: (User?) -> Unit)
  fun saveVehicle(userId: String, vehicle: Vehicle, callback: (String) -> Unit)
}
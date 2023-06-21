package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.User
import com.google.android.gms.tasks.Task

interface UserRepository {
  fun createUser(user: User, callback: (task: Task<Void>) -> Unit)
  fun getUser(userId: String, callback: (User?) -> Unit)
}
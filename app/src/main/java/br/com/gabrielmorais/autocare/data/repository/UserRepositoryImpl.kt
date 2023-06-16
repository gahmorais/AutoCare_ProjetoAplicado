package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

class UserRepositoryImpl(private val database: FirebaseDatabase) : UserRepository {
  override fun createUser(user: User, callback: (task: Task<Void>) -> Unit) {
    database.reference
      .child("Usuarios")
      .push()
      .setValue(user).addOnCompleteListener { callback(it) }
  }
}
package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase


private const val USER_CHILD = "Usuários"

class UserRepositoryImpl(private val database: FirebaseDatabase) : UserRepository {
  override fun createUser(user: User, callback: (task: Task<Void>) -> Unit) {
    database.reference
      .child(USER_CHILD)
      .child(user.id)
      .setValue(user).addOnCompleteListener { callback(it) }
  }

  override fun getUser(userId: String, callback: (User?) -> Unit) {
    database
      .reference
      .child(USER_CHILD)
      .child(userId)
      .get()
      .addOnSuccessListener { data ->
        callback(data.value as User?)
      }
      .addOnFailureListener { error ->
        throw error
      }
  }


}
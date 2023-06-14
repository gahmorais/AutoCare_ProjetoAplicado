package br.com.gabrielmorais.autocare.ui.authorization

import br.com.gabrielmorais.autocare.ui.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  val currentUser: FirebaseUser?
  fun login(email: String, password: String): Flow<Resource<AuthResult>>
  fun register(email: String, password: String): Flow<Resource<AuthResult>>
}
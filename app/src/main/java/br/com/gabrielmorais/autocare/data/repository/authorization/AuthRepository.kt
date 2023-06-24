package br.com.gabrielmorais.autocare.data.repository.authorization


import br.com.gabrielmorais.autocare.data.repository.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  fun getCurrentUser(): FirebaseUser?
  fun login(email: String, password: String): Flow<Resource<AuthResult?>>
  fun register(email: String, password: String): Flow<Resource<AuthResult>>
  fun logout()
  fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit)
}
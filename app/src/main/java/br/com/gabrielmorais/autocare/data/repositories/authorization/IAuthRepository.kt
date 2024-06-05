package br.com.gabrielmorais.autocare.data.repositories.authorization


import br.com.gabrielmorais.autocare.data.repositories.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
  fun getCurrentUser(): FirebaseUser?
  fun login(email: String, password: String): Flow<Resource<AuthResult?>>
  fun register(email: String, password: String): Flow<Resource<AuthResult>>
  fun logout()
  fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit)
  fun changePassword(email: String, callback: (String) -> Unit)
}
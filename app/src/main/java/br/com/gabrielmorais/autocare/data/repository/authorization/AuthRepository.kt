package br.com.gabrielmorais.autocare.data.repository.authorization


import br.com.gabrielmorais.autocare.data.repository.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  fun getCurrentUser(): FirebaseUser?

  /**
   * uid da sessao ativa. Exposto separado de [getCurrentUser] porque e tudo que
   * as ViewModels precisam, e assim elas nao dependem de FirebaseUser - que e
   * praticamente impossivel de instanciar em teste unitario.
   */
  fun currentUserId(): String?
  fun login(email: String, password: String): Flow<Resource<AuthResult?>>
  fun register(email: String, password: String): Flow<Resource<AuthResult>>
  fun logout()
  fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit)
  fun changePassword(email: String, callback: (String) -> Unit, onError: (Throwable) -> Unit)
}
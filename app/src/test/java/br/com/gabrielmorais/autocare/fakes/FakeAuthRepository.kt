package br.com.gabrielmorais.autocare.fakes

import br.com.gabrielmorais.autocare.data.repository.Resource
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeAuthRepository(
  private var userId: String? = "uid-teste"
) : AuthRepository {

  var loggedOut = false
  var passwordResetEmails = mutableListOf<String>()
  var changePasswordFailure: Throwable? = null

  fun signOutForTest() {
    userId = null
  }

  override fun getCurrentUser(): FirebaseUser? = null

  override fun currentUserId(): String? = userId

  override fun login(email: String, password: String): Flow<Resource<AuthResult?>> =
    flow { emit(Resource.success(null)) }

  override fun register(email: String, password: String): Flow<Resource<AuthResult>> =
    flow { emit(Resource.error(null, "não usado nos testes")) }

  override fun logout() {
    loggedOut = true
    userId = null
  }

  override fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit) = Unit

  override fun changePassword(
    email: String,
    callback: (String) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    changePasswordFailure?.let { return onError(it) }
    passwordResetEmails.add(email)
    callback("Email enviado")
  }
}

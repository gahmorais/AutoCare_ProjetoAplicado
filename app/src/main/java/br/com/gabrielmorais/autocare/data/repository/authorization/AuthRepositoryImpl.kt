package br.com.gabrielmorais.autocare.data.repository.authorization


import br.com.gabrielmorais.autocare.data.repository.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {


  override fun login(email: String, password: String): Flow<Resource<AuthResult?>> {
    return flow {
      emit(Resource.loading(null))
      val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
      emit(Resource.success(result))
    }.catch { error ->
      emit(Resource.error(null, error.message ?: "Ocorreu um erro"))
    }
  }

  override fun register(email: String, password: String): Flow<Resource<AuthResult>> {
    return flow {
      emit(Resource.loading(null))
      val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
      emit(Resource.success(result))
    }.catch { error ->
      emit(Resource.error(null, error.message ?: "Ocorreu um erro"))
    }
  }

  override fun getCurrentUser() = firebaseAuth.currentUser

  override fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit) =
    firebaseAuth.addAuthStateListener {
      callback(it)
    }

  override fun changePassword(email: String, callback: (String) -> Unit) {
    firebaseAuth.sendPasswordResetEmail(email)
      .addOnSuccessListener {
        callback("Email enviado")
      }
      .addOnFailureListener { error ->
        throw error
      }
  }


  override fun logout() {
    firebaseAuth.signOut()
  }
}
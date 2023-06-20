package br.com.gabrielmorais.autocare.data.repository.authorization

import br.com.gabrielmorais.autocare.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {


  override fun login(email: String, password: String): Flow<Resource<AuthResult>> {
    return flow {
      emit(Resource.Loading())
      val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
      emit(Resource.Success(result))
    }.catch {
      emit(Resource.Error(it.message.toString()))
    }
  }

  override fun register(email: String, password: String): Flow<Resource<AuthResult>> {
    return flow {
      emit(Resource.Loading())
      val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
      emit(Resource.Success(result))
    }.catch {
      emit(Resource.Error(it.message.toString()))
    }
  }

  override fun getCurrentUser() = firebaseAuth.currentUser

  override fun getCurrentUserListener(callback: (firebaseAuth: FirebaseAuth) -> Unit) =
    firebaseAuth.addAuthStateListener {
      callback(it)
    }


  override fun logout() {
    firebaseAuth.signOut()
  }
}
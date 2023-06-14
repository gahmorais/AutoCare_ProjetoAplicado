package br.com.gabrielmorais.autocare.ui.authorization

import br.com.gabrielmorais.autocare.ui.utils.Resource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val firebaseAuth: FirebaseAuth) : AuthRepository {
  override val currentUser: FirebaseUser?
    get() = firebaseAuth.currentUser

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
}
package br.com.gabrielmorais.autocare.ui.authorization

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Auth private constructor() {
  private val auth = Firebase.auth

  companion object {
    private var instance: Auth? = null
    fun getInstance(): Auth {
      return instance ?: Auth().also { instance = it }
    }
  }

  fun login(email: String, password: String) = try {
    auth.signInWithEmailAndPassword(email, password)
  } catch (e: Exception) {
    e.printStackTrace()
    throw e
  }

}
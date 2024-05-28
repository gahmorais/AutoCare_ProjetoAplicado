package br.com.gabrielmorais.autocare.data.repository.authorization

import android.content.Context
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.repository.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class AuthRepository(
  context: Context,
  private val database: AppDatabase
) {
  private val preferences = context.getSharedPreferences("authorization", Context.MODE_PRIVATE)
  private val editor = preferences.edit()
  private val CURRENT_USER = "user"

  fun getCurrentUser(): User? {
    val currentUser = preferences.getString(CURRENT_USER, "")
    return Gson().fromJson(currentUser, User::class.java)
  }

  suspend fun login(nickname: String, password: String): Flow<Resource<User?>> {
    return flow {
      emit(Resource.loading(null))
      val user = database.userDao().getUserByNickname(nickname)
        ?: throw Exception("Usuário ou senha incorretos")
      val isNotCorrect = user.password != password
      if (isNotCorrect) throw Exception("Usuário ou senha incorretos")
      val jsonUser = Gson().toJson(user)
      editor.putString(CURRENT_USER, jsonUser)
      editor.apply()
      emit(Resource.success(user))
    }.catch { error ->
      emit(Resource.error(null, error.message ?: "Ocorreu um erro"))
    }
  }

  fun logout() {
    editor.putString(CURRENT_USER, "")
    editor.apply()
  }

}
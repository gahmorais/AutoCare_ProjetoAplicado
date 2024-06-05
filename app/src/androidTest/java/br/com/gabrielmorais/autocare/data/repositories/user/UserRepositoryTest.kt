package br.com.gabrielmorais.autocare.data.repositories.user

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.gabrielmorais.autocare.data.AppDatabase
import br.com.gabrielmorais.autocare.data.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserRepositoryTest {
  private lateinit var userRepository: UserRepository
  private lateinit var database: AppDatabase
  private val user = User(
    nickname = "gabriel.morais",
    name = "Gabriel Morais",
    password = "1234"
  )

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    userRepository = UserRepository(database.userDao())
  }

  @After
  fun closeDb() {
    database.close()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun createUser() = runTest {
    val userCreated = withContext(testScheduler) {
      userRepository.createUser(user)
      userRepository.getById(user.id)
    }
    advanceUntilIdle()
    Assert.assertEquals(userCreated, user)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getById() = runTest {
    val userFounded = withContext(testScheduler) {
      userRepository.createUser(user)
      userRepository.getById(user.id)
    }
    advanceUntilIdle()
    Assert.assertEquals(userFounded, user)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun getUserByNickname() = runTest {
    val userFounded = withContext(testScheduler) {
      userRepository.createUser(user)
      userRepository.getByNickName(user.nickname)
    }
    advanceUntilIdle()
    Assert.assertEquals(user, userFounded)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun updateUser() = runTest {
    val newNickName = "g.morais"
    val userUpdated = withContext(testScheduler) {
      userRepository.createUser(user)
      userRepository.update(user.copy(nickname = newNickName))
      userRepository.getById(user.id)
    }
    advanceUntilIdle()

    Assert.assertEquals(newNickName, userUpdated.nickname)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun deleteUser() = runTest {
    val userDeleted = withContext(testScheduler) {
      userRepository.createUser(user)
      userRepository.delete(user)
      userRepository.getById(user.id)
    }
    advanceUntilIdle()
    Assert.assertEquals(userDeleted, null)
  }

}
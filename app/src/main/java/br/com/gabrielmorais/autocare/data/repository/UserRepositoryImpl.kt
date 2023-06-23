package br.com.gabrielmorais.autocare.data.repository

import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.UserFirebase
import br.com.gabrielmorais.autocare.data.models.Vehicle
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue


private const val USER_CHILD = "Usuarios"
private const val VEHICLE_CHILD = "vehicles"

class UserRepositoryImpl(private val database: FirebaseDatabase) : UserRepository {
  override fun createUser(user: User, callback: (task: Task<Void>) -> Unit) {
    database.reference
      .child(USER_CHILD)
      .child(user.id ?: "")
      .setValue(user).addOnCompleteListener { callback(it) }
  }

  override fun getUser(userId: String, callback: (User?) -> Unit) {
    database
      .reference
      .child(USER_CHILD)
      .child(userId)
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          try {

            val user = snapshot.getValue<UserFirebase>()
            val vehicleChildren = snapshot.child(VEHICLE_CHILD).children
            val iterator = vehicleChildren.iterator()
            val vehicleList = mutableListOf<Vehicle>()

            while (iterator.hasNext()) {
              val i = iterator.next()
              val brand = i.child("brand").getValue<String>()
              val model = i.child("model").getValue<String>()
              val plate = i.child("plate").getValue<String>()
              val id = i.child("id").getValue<String>()
              val nickName = i.child("nickName").getValue<String>()

              val vehicle = Vehicle(
                id = id,
                brand = brand,
                model = model,
                plate = plate,
                nickName = nickName
              )

              vehicleList.add(vehicle)

            }

            val newUser = User(
              id = user?.id,
              photo = user?.photo,
              email = user?.email,
              name = user?.name,
              vehicles = vehicleList
            )

            callback(newUser)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }

        override fun onCancelled(error: DatabaseError) {
          TODO("Not yet implemented")
        }
      })
  }

  override fun saveVehicle(userId: String, vehicle: Vehicle, callback: (String) -> Unit) {
    database
      .reference
      .child(USER_CHILD)
      .child(userId)
      .child(VEHICLE_CHILD)
      .child(vehicle.id!!)
      .setValue(vehicle)
      .addOnSuccessListener {
        callback("Veiculo salvo com sucesso")
      }
      .addOnFailureListener { error ->
        throw error
      }
  }
}
package br.com.gabrielmorais.autocare.data.repository.services

import android.util.Log
import br.com.gabrielmorais.autocare.data.models.Service
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

const val LIST_SERVICE_CHILD = "lista-servicos"

class ServicesRepositoryImpl(private val database: FirebaseDatabase) : ServicesRepository {
  override fun getServices(
    onSuccess: (services: List<Service?>) -> Unit,
    onError: (Throwable) -> Unit
  ) {
    database.reference
      .child(LIST_SERVICE_CHILD)
      .get()
      .addOnSuccessListener { snapshot ->
        val snapshotChildren = snapshot.children
        val services = snapshotChildren.map { it.getValue<Service>() }
        onSuccess(services)
        Log.i("ServicesRepositoryImpl", "getServices: $services")
      }
      .addOnFailureListener {
        onError(it)
      }
  }

}
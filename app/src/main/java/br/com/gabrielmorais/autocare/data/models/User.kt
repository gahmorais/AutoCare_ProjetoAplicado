package br.com.gabrielmorais.autocare.data.models

data class User(
  val id: String? = null,
  val photo: String? = null,
  val email: String? = null,
  val name: String? = null,
  val vehicles: List<Vehicle>? = null
)

data class UserFirebase(
  val id: String? = null,
  val photo: String? = null,
  val email: String? = null,
  val name: String? = null,
  val vehicles: HashMap<String, Any>? = null
)

package br.com.gabrielmorais.autocare.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.koin.dsl.module

val firebaseModule = module {
  single { Firebase.auth }
  single { Firebase.database }
  single { Firebase.storage }
}
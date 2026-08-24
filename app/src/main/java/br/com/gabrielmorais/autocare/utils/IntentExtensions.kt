package br.com.gabrielmorais.autocare.utils

import android.content.Intent
import android.os.Build
import android.os.Parcelable

/**
 * `getParcelableExtra(String)` foi depreciado no Android 13 em favor da versao
 * com a classe explicita. Vive aqui porque tanto o receiver da notificacao
 * quanto a tela de manutencao precisam ler um Parcelable de um extra.
 */
@Suppress("DEPRECATION")
inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(key: String): T? =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    getParcelableExtra(key, T::class.java)
  } else {
    getParcelableExtra(key)
  }

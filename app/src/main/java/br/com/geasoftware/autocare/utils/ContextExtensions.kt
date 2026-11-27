package br.com.geasoftware.autocare.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * A Activity por tras do LocalContext do Compose. Ele pode vir embrulhado em
 * ContextWrapper (tema, dialogo), entao um cast direto falharia.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

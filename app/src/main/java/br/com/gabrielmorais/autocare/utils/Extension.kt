package br.com.gabrielmorais.autocare.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

fun Throwable.handleException(callback: (String) -> Unit) {
  printStackTrace()
  message?.let(callback)
}
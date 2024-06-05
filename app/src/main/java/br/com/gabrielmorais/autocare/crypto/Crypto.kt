package br.com.gabrielmorais.autocare.crypto

import java.security.MessageDigest

fun encrypt(key: String): String {

  val md = MessageDigest.getInstance("SHA-256")
  val bytes = md.digest(key.toByteArray())
  val hexString = StringBuffer()
  val salt = 0x100

  for (byte in bytes) {
    hexString.append(
      Integer.toHexString(
        (0xFF and byte.toInt()) + salt
      )
    )
  }

  return hexString.toString()
}
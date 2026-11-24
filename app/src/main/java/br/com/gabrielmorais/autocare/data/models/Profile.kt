package br.com.gabrielmorais.autocare.data.models

import java.util.UUID

/**
 * Pessoa dona de veiculos neste aparelho. Nao e uma conta: nao ha login nem
 * nada fora do aparelho. Existe para quem cuida dos carros da familia manter
 * separado o que e de cada um.
 *
 * [photoPath] e relativo ao filesDir (ex.: "images/<uuid>.jpg"), nunca absoluto:
 * o caminho absoluto muda entre aparelhos e quebraria backup e importacao.
 */
data class Profile(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val photoPath: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)

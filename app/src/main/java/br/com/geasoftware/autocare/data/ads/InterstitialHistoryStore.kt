package br.com.geasoftware.autocare.data.ads

import android.content.Context
import java.time.Instant
import java.time.LocalDate

/**
 * Guarda o [InterstitialHistory] em SharedPreferences: sao quatro numeros lidos
 * e gravados na thread principal, no momento de salvar uma manutencao, e o
 * DataStore obrigaria a suspender ali.
 */
class InterstitialHistoryStore(context: Context) {
  private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

  fun load(): InterstitialHistory = InterstitialHistory(
    sessions = prefs.getInt(SESSIONS, 0),
    lastShownAt = prefs.getLong(LAST_SHOWN_AT, NONE).takeIf { it != NONE }?.let(Instant::ofEpochMilli),
    shownDay = prefs.getLong(SHOWN_DAY, NONE).takeIf { it != NONE }?.let(LocalDate::ofEpochDay),
    shownOnDay = prefs.getInt(SHOWN_ON_DAY, 0)
  )

  fun save(history: InterstitialHistory) {
    prefs.edit()
      .putInt(SESSIONS, history.sessions)
      .putLong(LAST_SHOWN_AT, history.lastShownAt?.toEpochMilli() ?: NONE)
      .putLong(SHOWN_DAY, history.shownDay?.toEpochDay() ?: NONE)
      .putInt(SHOWN_ON_DAY, history.shownOnDay)
      .apply()
  }

  private companion object {
    const val FILE = "ads"
    const val SESSIONS = "sessions"
    const val LAST_SHOWN_AT = "last_shown_at"
    const val SHOWN_DAY = "shown_day"
    const val SHOWN_ON_DAY = "shown_on_day"
    const val NONE = Long.MIN_VALUE
  }
}

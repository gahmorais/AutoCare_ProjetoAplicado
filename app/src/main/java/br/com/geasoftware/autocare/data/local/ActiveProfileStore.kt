package br.com.geasoftware.autocare.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Qual perfil esta em uso. Guardado a parte do banco porque e preferencia do
 * aparelho, nao dado do usuario: nao entra na exportacao (etapa 8).
 *
 * O id pode apontar para um perfil que nao existe mais (excluido, ou backup
 * restaurado sem ele); quem le resolve para o primeiro perfil existente.
 */
interface ActiveProfileStore {
  val activeProfileId: Flow<String?>
  suspend fun setActiveProfileId(id: String)
}

/** Arquivo "settings" do DataStore; o nome entra nas regras de backup (etapa 7). */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreActiveProfileStore(
  private val dataStore: DataStore<Preferences>
) : ActiveProfileStore {

  override val activeProfileId: Flow<String?> =
    dataStore.data.map { preferences -> preferences[KEY] }

  override suspend fun setActiveProfileId(id: String) {
    dataStore.edit { preferences -> preferences[KEY] = id }
  }

  private companion object {
    val KEY = stringPreferencesKey("active_profile_id")
  }
}

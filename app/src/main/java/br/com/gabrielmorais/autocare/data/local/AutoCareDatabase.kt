package br.com.gabrielmorais.autocare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.com.gabrielmorais.autocare.data.local.dao.MaintenanceDao
import br.com.gabrielmorais.autocare.data.local.dao.ProfileDao
import br.com.gabrielmorais.autocare.data.local.dao.VehicleDao

/**
 * Toda mudanca de entidade sobe [version] e precisa de uma Migration testada:
 * o esquema de cada versao fica em app/schemas/ e e versionado no git. Nada de
 * fallbackToDestructiveMigration - aqui o banco e o unico lugar onde os dados
 * do usuario existem.
 */
@Database(
  entities = [ProfileEntity::class, VehicleEntity::class, MaintenanceEntity::class],
  version = 1,
  exportSchema = true
)
abstract class AutoCareDatabase : RoomDatabase() {
  abstract fun profileDao(): ProfileDao
  abstract fun vehicleDao(): VehicleDao
  abstract fun maintenanceDao(): MaintenanceDao

  companion object {
    /** Referenciado tambem pelas regras de backup (etapa 7 do plano). */
    const val NAME = "autocare.db"

    fun build(context: Context): AutoCareDatabase =
      Room.databaseBuilder(context.applicationContext, AutoCareDatabase::class.java, NAME)
        .build()
  }
}

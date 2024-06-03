package br.com.gabrielmorais.autocare.data

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.gabrielmorais.autocare.data.dao.MaintenanceDao
import br.com.gabrielmorais.autocare.data.dao.UserDao
import br.com.gabrielmorais.autocare.data.dao.VehicleDao
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle

@Database(
  entities = [Maintenance::class, User::class, Vehicle::class],
  version = 1
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun maintenanceDao(): MaintenanceDao
  abstract fun vehicleDao(): VehicleDao
}


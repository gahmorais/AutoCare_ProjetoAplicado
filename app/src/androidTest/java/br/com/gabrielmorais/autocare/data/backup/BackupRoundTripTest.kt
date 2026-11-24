package br.com.gabrielmorais.autocare.data.backup

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.local.AutoCareDatabase
import br.com.gabrielmorais.autocare.data.local.repository.RoomMaintenanceRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomProfileRepository
import br.com.gabrielmorais.autocare.data.local.repository.RoomVehicleRepository
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * O caminho inteiro no aparelho: Room real, fotos JPEG reais e o
 * [BitmapImportedImageCheck] de producao, que na JVM nao tem como rodar.
 */
@RunWith(AndroidJUnit4::class)
class BackupRoundTripTest {

  private val context = InstrumentationRegistry.getInstrumentation().targetContext
  private val workDir = File(context.cacheDir, "backup-round-trip-test")
  private val files = File(workDir, "files")
  private lateinit var database: AutoCareDatabase

  private val profilePhoto = "images/11111111-1111-1111-1111-111111111111.jpg"
  private val vehiclePhoto = "images/22222222-2222-2222-2222-222222222222.jpg"

  @Before
  fun setUp() {
    workDir.deleteRecursively()
    files.mkdirs()
    database = Room.inMemoryDatabaseBuilder(context, AutoCareDatabase::class.java).build()
  }

  @After
  fun tearDown() {
    database.close()
    workDir.deleteRecursively()
  }

  private fun jpeg(width: Int, height: Int): ByteArray {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.RED) }
    return ByteArrayOutputStream().use { out ->
      bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
      bitmap.recycle()
      out.toByteArray()
    }
  }

  private fun importer(store: BackupStore = RoomBackupStore(database)) = BackupImporter(
    store = store,
    imagesRoot = files,
    stagingRoot = File(workDir, "staging"),
    imageCheck = BitmapImportedImageCheck(context.contentResolver)
  )

  @Test
  fun exportClearImport_restoresDataAndPhotosOpen() = runBlocking {
    File(files, profilePhoto).apply { parentFile!!.mkdirs(); writeBytes(jpeg(400, 400)) }
    File(files, vehiclePhoto).writeBytes(jpeg(1280, 720))
    val profiles = RoomProfileRepository(database)
    val vehicles = RoomVehicleRepository(database)
    val maintenances = RoomMaintenanceRepository(database)
    profiles.saveProfile(Profile(id = "ana", name = "Ana", photoPath = profilePhoto, createdAt = 1L))
    vehicles.saveVehicle(Vehicle(id = "gol", profileId = "ana", nickName = "Gol", photo = vehiclePhoto))
    maintenances.saveMaintenance("gol", Maintenance(id = 7, description = "Óleo", completed = true))

    val backup = ByteArrayOutputStream()
      .also { BackupExporter(RoomBackupStore(database), files, "1.0").export(it) }
      .toByteArray()

    // "Aparelho novo": banco vazio e nenhuma foto.
    profiles.deleteProfile("ana")
    File(files, "images").deleteRecursively()
    assertTrue(profiles.observeProfiles().first().isEmpty())

    val importer = importer()
    val prepared = importer.prepare(ByteArrayInputStream(backup))
    assertEquals(BackupPreview(newProfiles = 1, newVehicles = 1, newMaintenances = 1), prepared.preview)
    importer.apply(prepared)

    val gol = vehicles.getVehicle("gol")!!
    assertEquals("Gol", gol.nickName)
    assertEquals(listOf(7), gol.maintenances!!.map { it.id })
    assertTrue(gol.maintenances!!.single().completed)
    listOf(profilePhoto, vehiclePhoto).forEach { path ->
      assertNotNull("a foto $path tem que abrir", BitmapFactory.decodeFile(File(files, path).path))
    }
  }

  @Test
  fun oversizedImportedPhoto_isRecompressedWithinThePolicy() = runBlocking {
    val snapshot = BackupSnapshot(
      profiles = listOf(Profile(id = "ana", name = "Ana", photoPath = profilePhoto, createdAt = 1L)),
      vehicles = listOf(Vehicle(id = "gol", profileId = "ana", photo = vehiclePhoto, maintenances = emptyList()))
    )
    val zip = zip(
      BackupFormat.MANIFEST to BackupSerializer.encode(snapshot, java.time.Instant.EPOCH, "1").toByteArray(),
      profilePhoto to jpeg(2000, 1000),
      vehiclePhoto to jpeg(4000, 3000)
    )

    val importer = importer()
    importer.apply(importer.prepare(ByteArrayInputStream(zip)))

    fun bounds(path: String) = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      .also { BitmapFactory.decodeFile(File(files, path).path, it) }
    assertEquals(ImageKind.PROFILE_PHOTO.maxDimension, maxOf(bounds(profilePhoto).outWidth, bounds(profilePhoto).outHeight))
    assertEquals(ImageKind.VEHICLE_PHOTO.maxDimension, maxOf(bounds(vehiclePhoto).outWidth, bounds(vehiclePhoto).outHeight))
  }

  @Test
  fun jpegMarkerWithGarbage_isRejected() = runBlocking {
    val snapshot = BackupSnapshot(
      profiles = listOf(Profile(id = "ana", name = "Ana", photoPath = profilePhoto, createdAt = 1L)),
      vehicles = emptyList()
    )
    val garbage = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()) + ByteArray(500) { 7 }
    val zip = zip(
      BackupFormat.MANIFEST to BackupSerializer.encode(snapshot, java.time.Instant.EPOCH, "1").toByteArray(),
      profilePhoto to garbage
    )

    try {
      importer().prepare(ByteArrayInputStream(zip))
      fail("uma foto que nao decodifica deve ser recusada")
    } catch (expected: BackupException.Corrupted) {
    }
    assertTrue(RoomProfileRepository(database).observeProfiles().first().isEmpty())
    assertTrue(File(files, "images").listFiles().isNullOrEmpty())
  }

  /** Room de verdade: o upsert em lote nao pode apagar em cascata o que ja existe. */
  @Test
  fun mergeOverExistingRows_keepsChildrenThatAreOnlyOnTheDevice() = runBlocking {
    val vehicles = RoomVehicleRepository(database)
    RoomProfileRepository(database).saveProfile(Profile(id = "ana", name = "Ana", createdAt = 1L))
    vehicles.saveVehicle(Vehicle(id = "gol", profileId = "ana"))
    vehicles.saveVehicle(Vehicle(id = "uno", profileId = "ana"))
    RoomMaintenanceRepository(database).saveMaintenance("uno", Maintenance(id = 1))

    RoomBackupStore(database).merge(
      BackupSnapshot(
        profiles = listOf(Profile(id = "ana", name = "Ana nova", createdAt = 1L)),
        vehicles = listOf(Vehicle(id = "gol", profileId = "ana", nickName = "Gol", maintenances = emptyList()))
      )
    )

    assertEquals(listOf(1), vehicles.getVehicle("uno")!!.maintenances!!.map { it.id })
    assertEquals("Gol", vehicles.getVehicle("gol")!!.nickName)
  }

  private fun zip(vararg entries: Pair<String, ByteArray>): ByteArray =
    ByteArrayOutputStream().also { bytes ->
      ZipOutputStream(bytes).use { zip ->
        entries.forEach { (name, content) ->
          zip.putNextEntry(ZipEntry(name))
          zip.write(content)
          zip.closeEntry()
        }
      }
    }.toByteArray()
}

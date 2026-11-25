package br.com.gabrielmorais.autocare.data.backup

import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.time.Instant

/**
 * Exportador e importador reais, com java.util.zip e org.json de verdade; so o
 * banco (Room nao roda na JVM) e a decodificacao de imagem sao fakes.
 */
class BackupImporterTest {

  @get:Rule
  val temp = TemporaryFolder()

  private lateinit var sourceFiles: File
  private lateinit var deviceFiles: File
  private lateinit var staging: File

  private val ana = Profile(id = "ana", name = "Ana", photoPath = photoPath(1), createdAt = 1L)
  private val pedro = Profile(id = "pedro", name = "Pedro", createdAt = 2L)
  private val oil = Maintenance(
    id = 10,
    description = "Troca de óleo",
    date = 19_000L,
    currentMileage = 50_000,
    forecastNextExchangeMileage = 60_000,
    forecastNextExchangeDate = 19_180L,
    comments = "Óleo 5W30 \"sintético\"\nlinha 2",
    completed = true
  )
  private val tires = Maintenance(id = -7, description = "Pneus")

  @Before
  fun setUp() {
    sourceFiles = temp.newFolder("source")
    deviceFiles = temp.newFolder("device")
    staging = File(temp.root, "cache/backup-import")
  }

  private fun storePhoto(root: File, path: String, bytes: ByteArray = jpeg()) =
    File(root, path).apply { parentFile!!.mkdirs(); writeBytes(bytes) }

  private fun exporter(store: BackupStore, root: File = sourceFiles) = BackupExporter(
    store = store,
    imagesRoot = root,
    appVersion = "1.0.0",
    ioDispatcher = Dispatchers.Unconfined,
    clock = { Instant.parse("2026-11-24T12:00:00Z") }
  )

  private fun importer(
    store: BackupStore,
    check: ImportedImageCheck = FakeImageCheck(),
    limits: BackupLimits = BackupLimits()
  ) = BackupImporter(
    store = store,
    imagesRoot = deviceFiles,
    stagingRoot = staging,
    imageCheck = check,
    limits = limits,
    ioDispatcher = Dispatchers.Unconfined
  )

  private fun export(snapshot: BackupSnapshot): ByteArray = runBlocking {
    ByteArrayOutputStream().also { exporter(FakeBackupStore(snapshot)).export(it) }.toByteArray()
  }

  private fun manifest(json: String) = zipOf(BackupFormat.MANIFEST to json.toByteArray())

  private inline fun <reified T : Throwable> assertFails(block: () -> Unit): T {
    try {
      block()
    } catch (error: Throwable) {
      if (error is T) return error
      throw AssertionError("Esperava ${T::class.simpleName}, veio $error", error)
    }
    fail("Esperava ${T::class.simpleName}")
    throw IllegalStateException()
  }

  private fun deviceImages(): List<String> =
    File(deviceFiles, "images").listFiles()?.map { it.name }?.sorted().orEmpty()

  private fun stagingFiles(): List<File> = staging.walk().filter { it.isFile }.toList()

  private val family = BackupSnapshot(
    profiles = listOf(ana, pedro),
    vehicles = listOf(
      vehicle("gol", "ana", oil, tires, photo = photoPath(2)),
      vehicle("uno", "pedro")
    )
  )

  // --- ida e volta ---------------------------------------------------------

  @Test
  fun exportThenImportIntoEmptyDevice_restoresRecordsAndPhotos() = runBlocking {
    storePhoto(sourceFiles, photoPath(1), jpeg(1))
    storePhoto(sourceFiles, photoPath(2), jpeg(2))
    val device = FakeBackupStore()
    val importer = importer(device)

    val prepared = importer.prepare(ByteArrayInputStream(export(family)))
    val imported = importer.apply(prepared)

    assertEquals(family.normalized(), device.current)
    assertEquals(setOf(10, -7), imported.map { it.id }.toSet())
    assertArrayEquals(jpeg(1), File(deviceFiles, photoPath(1)).readBytes())
    assertArrayEquals(jpeg(2), File(deviceFiles, photoPath(2)).readBytes())
    assertTrue("a pasta temporaria deve sumir", stagingFiles().isEmpty())
  }

  @Test
  fun exportedPhotoMissingOnDisk_isExportedAsNoPhoto() = runBlocking {
    storePhoto(sourceFiles, photoPath(2))
    val device = FakeBackupStore()
    val importer = importer(device)

    importer.apply(importer.prepare(ByteArrayInputStream(export(family))))

    assertNull(device.current.profiles.first { it.id == "ana" }.photoPath)
    assertEquals(photoPath(2), device.current.vehicles.first { it.id == "gol" }.photo)
  }

  @Test
  fun photosAreCheckedWithTheKindOfTheirOwner() = runBlocking {
    storePhoto(sourceFiles, photoPath(1))
    storePhoto(sourceFiles, photoPath(2))
    val check = FakeImageCheck()

    importer(FakeBackupStore(), check).prepare(ByteArrayInputStream(export(family)))

    assertEquals(
      setOf(photoPath(1) to ImageKind.PROFILE_PHOTO, photoPath(2) to ImageKind.VEHICLE_PHOTO),
      check.checked.toSet()
    )
  }

  // --- mescla --------------------------------------------------------------

  @Test
  fun importOverExistingData_updatesByIdWithoutDuplicating() = runBlocking {
    val device = FakeBackupStore(
      BackupSnapshot(
        profiles = listOf(ana.copy(name = "Ana antiga", photoPath = null)),
        vehicles = listOf(vehicle("gol", "ana", oil.copy(currentMileage = 1)))
      )
    )
    val importer = importer(device)

    importer.apply(importer.prepare(ByteArrayInputStream(export(family))))

    assertEquals(2, device.current.profiles.size)
    assertEquals("Ana", device.current.profiles.first { it.id == "ana" }.name)
    val gol = device.current.vehicles.single { it.id == "gol" }
    assertEquals(listOf(-7, 10), gol.maintenances!!.map { it.id })
    assertEquals(50_000, gol.maintenances!!.first { it.id == 10 }.currentMileage)
  }

  @Test
  fun importKeepsRecordsThatAreOnlyOnTheDevice() = runBlocking {
    val bia = Profile(id = "bia", name = "Bia", createdAt = 3L)
    val onlyHere = Maintenance(id = 99, description = "Só no aparelho")
    val device = FakeBackupStore(
      BackupSnapshot(
        profiles = listOf(ana, bia),
        vehicles = listOf(vehicle("gol", "ana", onlyHere), vehicle("fusca", "bia"))
      )
    )
    val importer = importer(device)

    importer.apply(importer.prepare(ByteArrayInputStream(export(family))))

    assertTrue(device.current.profiles.any { it.id == "bia" })
    assertTrue(device.current.vehicles.any { it.id == "fusca" })
    val gol = device.current.vehicles.single { it.id == "gol" }
    assertTrue(gol.maintenances!!.any { it.id == 99 })
  }

  // --- previa --------------------------------------------------------------

  @Test
  fun preview_countsNewAndChangedOnly_andWritesNothing() = runBlocking {
    storePhoto(sourceFiles, photoPath(1))
    val device = FakeBackupStore(
      BackupSnapshot(
        // ana igual, gol com apelido diferente, oil igual, tires nova
        profiles = listOf(ana),
        vehicles = listOf(vehicle("gol", "ana", oil, photo = photoPath(2)).copy(nickName = "Outro"))
      )
    )
    storePhoto(deviceFiles, photoPath(1))

    val prepared = importer(device).prepare(ByteArrayInputStream(export(family)))

    assertEquals(
      BackupPreview(
        newProfiles = 1,
        updatedProfiles = 0,
        newVehicles = 1,
        updatedVehicles = 1,
        newMaintenances = 1,
        updatedMaintenances = 0
      ),
      prepared.preview
    )
    assertEquals("a previa nao pode gravar no banco", 0, device.merges)
    assertEquals(listOf(photoPath(1).removePrefix("images/")), deviceImages())
  }

  @Test
  fun preview_movingAMaintenanceToAnotherVehicleCountsAsUpdate() = runBlocking {
    val device = FakeBackupStore(
      BackupSnapshot(
        profiles = listOf(ana, pedro),
        vehicles = listOf(vehicle("gol", "ana"), vehicle("uno", "pedro", tires))
      )
    )
    val file = BackupSnapshot(
      profiles = listOf(ana.copy(photoPath = null), pedro),
      vehicles = listOf(vehicle("gol", "ana", tires), vehicle("uno", "pedro"))
    )

    val preview = importer(device).prepare(ByteArrayInputStream(export(file))).preview

    assertEquals(1, preview.updatedMaintenances)
    assertEquals(0, preview.newMaintenances)
  }

  @Test
  fun reimportingTheSameFile_hasNothingToDo() = runBlocking {
    storePhoto(sourceFiles, photoPath(1))
    storePhoto(sourceFiles, photoPath(2))
    val file = export(family)
    val device = FakeBackupStore()
    val importer = importer(device)
    importer.apply(importer.prepare(ByteArrayInputStream(file)))

    val again = importer.prepare(ByteArrayInputStream(file))

    assertTrue(again.preview.isEmpty)
    importer.discard(again)
  }

  @Test
  fun cancelAfterPreview_leavesNoFilesBehind() = runBlocking {
    storePhoto(sourceFiles, photoPath(1))
    storePhoto(sourceFiles, photoPath(2))
    val device = FakeBackupStore()
    val importer = importer(device)

    val prepared = importer.prepare(ByteArrayInputStream(export(family)))
    assertFalse("a previa extrai as fotos para o cache", stagingFiles().isEmpty())
    importer.discard(prepared)

    assertTrue(stagingFiles().isEmpty())
    assertTrue(deviceImages().isEmpty())
    assertEquals(0, device.merges)
  }

  @Test
  fun leftoverFromAnInterruptedImport_isClearedOnTheNextOne() = runBlocking {
    val leftover = File(staging, "antiga/images/x.jpg").apply { parentFile!!.mkdirs(); writeBytes(jpeg()) }

    importer(FakeBackupStore()).prepare(ByteArrayInputStream(export(family)))

    assertFalse(leftover.exists())
  }

  // --- falha na aplicacao --------------------------------------------------

  @Test
  fun failureWhileMerging_removesCopiedPhotosAndKeepsTheDatabase() = runBlocking {
    storePhoto(sourceFiles, photoPath(1))
    storePhoto(sourceFiles, photoPath(2))
    storePhoto(deviceFiles, photoPath(2), jpeg(42)) // ja estava no aparelho
    val device = FakeBackupStore().apply { mergeFailure = IOException("disco cheio") }
    val importer = importer(device)
    val prepared = importer.prepare(ByteArrayInputStream(export(family)))

    assertFails<IOException> { runBlocking { importer.apply(prepared) } }

    assertEquals(listOf(photoPath(2).removePrefix("images/")), deviceImages())
    assertArrayEquals("a foto que ja existia fica intacta", jpeg(42), File(deviceFiles, photoPath(2)).readBytes())
    assertTrue(device.current.profiles.isEmpty())
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun photoAlreadyOnTheDevice_isNotOverwritten() = runBlocking {
    storePhoto(sourceFiles, photoPath(2), jpeg(1))
    storePhoto(deviceFiles, photoPath(2), jpeg(2))
    val importer = importer(FakeBackupStore())

    importer.apply(importer.prepare(ByteArrayInputStream(export(family))))

    assertArrayEquals(jpeg(2), File(deviceFiles, photoPath(2)).readBytes())
  }

  @Test
  fun imageInTheZipThatNoRecordUses_isNotCopied() = runBlocking {
    val json = BackupSerializer.encode(family.copy(profiles = listOf(ana.copy(photoPath = null), pedro),
      vehicles = family.vehicles.map { it.copy(photo = null) }), Instant.EPOCH, "1")
    val importer = importer(FakeBackupStore())

    importer.apply(importer.prepare(ByteArrayInputStream(zipOf(
      BackupFormat.MANIFEST to json.toByteArray(),
      photoPath(5) to jpeg()
    ))))

    assertTrue(deviceImages().isEmpty())
  }

  // --- arquivo hostil ------------------------------------------------------

  @Test
  fun zipSlipEntries_areRejected() {
    val manifest = BackupFormat.MANIFEST to "{}".toByteArray()
    listOf("../../x", "/etc/passwd", "images/../../x.jpg", "images/foto.png", "outra/coisa.jpg")
      .forEach { name ->
        assertFails<BackupException.Corrupted> {
          runBlocking { importer(FakeBackupStore()).prepare(ByteArrayInputStream(zipOf(manifest, name to jpeg()))) }
        }
        assertFalse(File(temp.root, "x").exists())
        assertTrue(stagingFiles().isEmpty())
      }
  }

  @Test
  fun newerSchemaVersion_asksToUpdateTheApp() {
    val error = assertFails<BackupException.NewerVersion> {
      runBlocking {
        importer(FakeBackupStore()).prepare(
          ByteArrayInputStream(manifest("""{"format":"autocare-backup","schemaVersion":2,"profiles":[]}"""))
        )
      }
    }
    assertEquals("Atualize o app para importar este arquivo", error.message)
  }

  @Test
  fun truncatedJson_abortsWithoutApplying() {
    val device = FakeBackupStore()
    val json = BackupSerializer.encode(family, Instant.EPOCH, "1")
    assertFails<BackupException.Corrupted> {
      runBlocking { importer(device).prepare(ByteArrayInputStream(manifest(json.take(json.length / 2)))) }
    }
    assertEquals(0, device.merges)
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun missingRequiredField_abortsWithoutApplying() {
    val device = FakeBackupStore()
    val json = """{"format":"autocare-backup","schemaVersion":1,"profiles":[{"id":"ana","createdAt":1}]}"""
    val error = assertFails<BackupException.Corrupted> {
      runBlocking { importer(device).prepare(ByteArrayInputStream(manifest(json))) }
    }
    assertTrue(error.message!!, error.message!!.contains("\"name\""))
    assertEquals(0, device.merges)
  }

  @Test
  fun truncatedZip_isRejected() {
    val file = export(family)
    assertFails<BackupException> {
      runBlocking { importer(FakeBackupStore()).prepare(ByteArrayInputStream(file.copyOf(file.size / 2))) }
    }
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun fileThatIsNotAZipOrHasNoManifest_isNotABackup() {
    listOf("texto qualquer".toByteArray(), zipOf(photoPath(1) to jpeg()), ByteArray(0)).forEach { bytes ->
      assertFails<BackupException.NotABackup> {
        runBlocking { importer(FakeBackupStore()).prepare(ByteArrayInputStream(bytes)) }
      }
    }
  }

  @Test
  fun imageThatIsNotJpeg_isRejected() {
    val json = BackupSerializer.encode(family, Instant.EPOCH, "1").toByteArray()
    assertFails<BackupException.Corrupted> {
      runBlocking {
        importer(FakeBackupStore()).prepare(
          ByteArrayInputStream(zipOf(BackupFormat.MANIFEST to json, photoPath(1) to "PNG?".toByteArray()))
        )
      }
    }
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun imageThatDoesNotDecode_isRejected() {
    storePhoto(sourceFiles, photoPath(1))
    val file = export(family)
    assertFails<BackupException.Corrupted> {
      runBlocking { importer(FakeBackupStore(), FakeImageCheck(failFor = setOf(photoPath(1)))).prepare(ByteArrayInputStream(file)) }
    }
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun photoReferencedButAbsentEverywhere_becomesNoPhoto() = runBlocking {
    val json = BackupSerializer.encode(family, Instant.EPOCH, "1")
    val device = FakeBackupStore()
    val importer = importer(device)

    importer.apply(importer.prepare(ByteArrayInputStream(manifest(json))))

    assertNull(device.current.profiles.first { it.id == "ana" }.photoPath)
    assertNull(device.current.vehicles.first { it.id == "gol" }.photo)
  }

  // --- limites -------------------------------------------------------------

  @Test
  fun tooManyEntries_isRejected() {
    val entries = (1..4).map { photoPath(it) to jpeg() }.toTypedArray()
    assertFails<BackupException.TooLarge> {
      runBlocking {
        importer(FakeBackupStore(), limits = BackupLimits(maxEntries = 3))
          .prepare(ByteArrayInputStream(zipOf(*entries)))
      }
    }
  }

  @Test
  fun manifestLargerThanTheLimit_isRejectedWhileDecompressing() {
    // Zeros comprimem para quase nada: e o formato do zip bomb.
    val bomb = zipOf(BackupFormat.MANIFEST to ByteArray(2 * 1024 * 1024))
    assertTrue(bomb.size < 10_000)
    assertFails<BackupException.TooLarge> {
      runBlocking {
        importer(FakeBackupStore(), limits = BackupLimits(maxManifestBytes = 1024 * 1024))
          .prepare(ByteArrayInputStream(bomb))
      }
    }
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun imageLargerThanTheLimit_isRejected() {
    assertFails<BackupException.TooLarge> {
      runBlocking {
        importer(FakeBackupStore(), limits = BackupLimits(maxImageBytes = 1024))
          .prepare(ByteArrayInputStream(zipOf(photoPath(1) to jpeg() + ByteArray(2048))))
      }
    }
    assertTrue(stagingFiles().isEmpty())
  }

  @Test
  fun totalLargerThanTheLimit_isRejected() {
    val entries = (1..5).map { photoPath(it) to ByteArray(1000) }.toTypedArray()
    assertFails<BackupException.TooLarge> {
      runBlocking {
        importer(FakeBackupStore(), limits = BackupLimits(maxImageBytes = 1000, maxTotalBytes = 4000))
          .prepare(ByteArrayInputStream(zipOf(*entries)))
      }
    }
  }
}

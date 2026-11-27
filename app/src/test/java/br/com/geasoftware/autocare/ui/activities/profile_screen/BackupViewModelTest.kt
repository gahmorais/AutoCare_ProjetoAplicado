package br.com.geasoftware.autocare.ui.activities.profile_screen

import android.net.Uri
import br.com.geasoftware.autocare.data.backup.BackupExporter
import br.com.geasoftware.autocare.data.backup.BackupImporter
import br.com.geasoftware.autocare.data.backup.BackupPreview
import br.com.geasoftware.autocare.data.backup.BackupSnapshot
import br.com.geasoftware.autocare.data.backup.FakeBackupStore
import br.com.geasoftware.autocare.data.backup.FakeImageCheck
import br.com.geasoftware.autocare.data.backup.vehicle
import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

  @get:Rule
  val temp = TemporaryFolder()

  private val exportUri = mock(Uri::class.java)
  private val importUri = mock(Uri::class.java)

  private val ana = Profile(id = "ana", name = "Ana", createdAt = 1L)
  private val oil = Maintenance(id = 1, description = "Óleo")
  private val family = BackupSnapshot(listOf(ana), listOf(vehicle("gol", "ana", oil)))

  /** "Documentos" em memoria, por Uri. */
  private inner class FakeStreams : DocumentStreams {
    val written = mutableMapOf<Uri, ByteArray>()
    val deleted = mutableListOf<Uri>()
    var outputFailure: IOException? = null

    override fun openInput(uri: Uri): InputStream =
      ByteArrayInputStream(written[uri] ?: throw IOException("não existe"))

    override fun openOutput(uri: Uri): OutputStream {
      outputFailure?.let { throw it }
      return object : ByteArrayOutputStream() {
        override fun close() {
          written[uri] = toByteArray()
        }
      }
    }

    override fun delete(uri: Uri) {
      deleted += uri
      written.remove(uri)
    }
  }

  private val streams = FakeStreams()
  private val imported = mutableListOf<List<Maintenance>>()
  private lateinit var source: FakeBackupStore
  private lateinit var device: FakeBackupStore
  private lateinit var staging: File

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    source = FakeBackupStore(family)
    device = FakeBackupStore()
    staging = File(temp.root, "staging")
  }

  @After
  fun tearDown() = Dispatchers.resetMain()

  private fun viewModel(store: FakeBackupStore) = BackupViewModel(
    exporter = BackupExporter(store, temp.newFolder(), "1.0", Dispatchers.Unconfined),
    importer = BackupImporter(
      store = store,
      imagesRoot = temp.newFolder(),
      stagingRoot = staging,
      imageCheck = FakeImageCheck(),
      ioDispatcher = Dispatchers.Unconfined
    ),
    streams = streams,
    sideEffects = { imported += it },
    today = { LocalDate.of(2026, 11, 24) }
  )

  private fun exportFamilyTo(uri: Uri) {
    viewModel(source).export(uri)
    streams.written[importUri] = streams.written.getValue(uri)
  }

  @Test
  fun suggestedFileName_hasTheDate() {
    assertEquals("autocare-backup-2026-11-24.zip", viewModel(source).suggestedFileName())
  }

  @Test
  fun export_writesTheFileAndReportsWhatWentIn() = runTest {
    val viewModel = viewModel(source)

    viewModel.export(exportUri)

    assertEquals(BackupResult.Exported(profiles = 1, vehicles = 1, maintenances = 1), viewModel.state.value.result)
    assertFalse(viewModel.state.value.busy)
    assertTrue(streams.written.getValue(exportUri).isNotEmpty())
  }

  @Test
  fun exportFailure_deletesTheHalfWrittenDocumentAndShowsAMessage() = runTest {
    streams.outputFailure = IOException("sem espaço")
    val viewModel = viewModel(source)

    viewModel.export(exportUri)

    assertEquals(listOf(exportUri), streams.deleted)
    assertEquals(BackupResult.Failed("Não foi possível exportar os dados"), viewModel.state.value.result)
  }

  @Test
  fun readImport_showsThePreviewAndDoesNotWrite() = runTest {
    exportFamilyTo(exportUri)
    val viewModel = viewModel(device)

    viewModel.readImport(importUri)

    assertEquals(BackupPreview(newProfiles = 1, newVehicles = 1, newMaintenances = 1), viewModel.state.value.preview)
    assertEquals(0, device.merges)
  }

  @Test
  fun confirmImport_mergesAndReschedulesTheReminders() = runTest {
    exportFamilyTo(exportUri)
    val viewModel = viewModel(device)
    viewModel.readImport(importUri)

    viewModel.confirmImport()

    assertEquals(1, device.merges)
    assertEquals(listOf(listOf(oil)), imported)
    assertNull(viewModel.state.value.preview)
    assertTrue(viewModel.state.value.result is BackupResult.Imported)
  }

  @Test
  fun cancelImport_writesNothingAndCleansUp() = runTest {
    exportFamilyTo(exportUri)
    val viewModel = viewModel(device)
    viewModel.readImport(importUri)

    viewModel.cancelImport()
    viewModel.confirmImport() // sem previa, nao faz nada

    assertNull(viewModel.state.value.preview)
    assertEquals(0, device.merges)
    assertTrue(imported.isEmpty())
    assertTrue(staging.walk().none { it.isFile })
  }

  @Test
  fun importingWhatIsAlreadyThere_saysThereIsNothingToDo() = runTest {
    exportFamilyTo(exportUri)
    val viewModel = viewModel(source)

    viewModel.readImport(importUri)

    assertNull(viewModel.state.value.preview)
    assertEquals(BackupResult.NothingToImport, viewModel.state.value.result)
  }

  @Test
  fun rejectedFile_showsTheReasonFromTheImporter() = runTest {
    streams.written[importUri] = "não é zip".toByteArray()
    val viewModel = viewModel(device)

    viewModel.readImport(importUri)

    assertEquals(BackupResult.Failed("Este arquivo não é um backup do AutoCare"), viewModel.state.value.result)
  }

  @Test
  fun failureWhileApplying_saysNothingChanged() = runTest {
    exportFamilyTo(exportUri)
    val viewModel = viewModel(device)
    viewModel.readImport(importUri)
    device.mergeFailure = IOException("disco cheio")

    viewModel.confirmImport()

    assertEquals(
      BackupResult.Failed("Não foi possível importar os dados. Nada foi alterado."),
      viewModel.state.value.result
    )
    assertTrue(imported.isEmpty())
  }

  @Test
  fun dismissResult_clearsIt() = runTest {
    val viewModel = viewModel(source)
    viewModel.export(exportUri)

    viewModel.dismissResult()

    assertNull(viewModel.state.value.result)
  }
}

package br.com.geasoftware.autocare.ui.activities.profile_screen

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.geasoftware.autocare.data.backup.BackupException
import br.com.geasoftware.autocare.data.backup.BackupExporter
import br.com.geasoftware.autocare.data.backup.BackupFormat
import br.com.geasoftware.autocare.data.backup.BackupImporter
import br.com.geasoftware.autocare.data.backup.BackupPreview
import br.com.geasoftware.autocare.data.models.Maintenance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

/** Abre o documento escolhido pelo seletor do sistema. */
interface DocumentStreams {
  fun openInput(uri: Uri): InputStream
  fun openOutput(uri: Uri): OutputStream

  /** Melhor esforco: apaga um documento que ficou pela metade. */
  fun delete(uri: Uri)
}

/** O que precisa acontecer fora do banco depois de uma importacao. */
fun interface ImportSideEffects {
  /** Cancela os lembretes das manutencoes importadas e reagenda os pendentes. */
  fun onImported(maintenances: List<Maintenance>)
}

enum class BackupOperation { EXPORTING, READING, IMPORTING }

sealed interface BackupResult {
  data class Exported(val profiles: Int, val vehicles: Int, val maintenances: Int) : BackupResult
  data class Imported(val preview: BackupPreview) : BackupResult
  data object NothingToImport : BackupResult
  data class Failed(val message: String) : BackupResult
}

data class BackupUiState(
  val operation: BackupOperation? = null,
  /** Previa a confirmar. Enquanto nao for null, ha uma importacao preparada. */
  val preview: BackupPreview? = null,
  val result: BackupResult? = null
) {
  val busy: Boolean get() = operation != null
}

/**
 * Exportar e importar o backup em .zip (etapa 8). A importacao sempre passa
 * pela previa: [readImport] so le e valida, [confirmImport] grava.
 */
class BackupViewModel(
  private val exporter: BackupExporter,
  private val importer: BackupImporter,
  private val streams: DocumentStreams,
  private val sideEffects: ImportSideEffects,
  private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

  private val _state = MutableStateFlow(BackupUiState())
  val state: StateFlow<BackupUiState> = _state.asStateFlow()

  private var prepared: BackupImporter.PreparedImport? = null

  fun suggestedFileName(): String = BackupFormat.fileName(today())

  fun export(uri: Uri) = run(BackupOperation.EXPORTING, onFailure = { streams.delete(uri) }) {
    val snapshot = streams.openOutput(uri).use { exporter.export(it) }
    _state.update {
      it.copy(
        result = BackupResult.Exported(
          profiles = snapshot.profiles.size,
          vehicles = snapshot.vehicles.size,
          maintenances = snapshot.maintenanceCount
        )
      )
    }
  }

  fun readImport(uri: Uri) = run(BackupOperation.READING) {
    discardPrepared()
    val ready = streams.openInput(uri).use { importer.prepare(it) }
    if (ready.preview.isEmpty) {
      importer.discard(ready)
      _state.update { it.copy(result = BackupResult.NothingToImport) }
    } else {
      prepared = ready
      _state.update { it.copy(preview = ready.preview) }
    }
  }

  fun confirmImport() {
    val ready = prepared ?: return
    prepared = null
    _state.update { it.copy(preview = null) }
    run(BackupOperation.IMPORTING) {
      val maintenances = importer.apply(ready)
      sideEffects.onImported(maintenances)
      _state.update { it.copy(result = BackupResult.Imported(ready.preview)) }
    }
  }

  fun cancelImport() {
    _state.update { it.copy(preview = null) }
    viewModelScope.launch { discardPrepared() }
  }

  fun dismissResult() = _state.update { it.copy(result = null) }

  private suspend fun discardPrepared() {
    val ready = prepared ?: return
    prepared = null
    runCatching { importer.discard(ready) }
  }

  private fun run(
    operation: BackupOperation,
    onFailure: () -> Unit = {},
    block: suspend () -> Unit
  ) {
    if (_state.value.busy) return
    _state.update { it.copy(operation = operation, result = null) }
    viewModelScope.launch {
      try {
        block()
      } catch (error: CancellationException) {
        throw error
      } catch (error: Exception) {
        runCatching(onFailure)
        _state.update { it.copy(result = BackupResult.Failed(messageFor(operation, error))) }
      } finally {
        _state.update { it.copy(operation = null) }
      }
    }
  }

  private fun messageFor(operation: BackupOperation, error: Exception): String = when {
    error is BackupException -> error.message.orEmpty()
    operation == BackupOperation.EXPORTING -> "Não foi possível exportar os dados"
    operation == BackupOperation.READING -> "Não foi possível ler o arquivo escolhido"
    else -> "Não foi possível importar os dados. Nada foi alterado."
  }

  // Sem onCleared: uma previa aberta quando a tela morre fica no cache e e
  // apagada no proximo prepare, que sempre limpa a pasta de importacao.
}

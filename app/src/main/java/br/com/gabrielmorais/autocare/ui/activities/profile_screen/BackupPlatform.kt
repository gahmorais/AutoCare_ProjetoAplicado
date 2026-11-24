package br.com.gabrielmorais.autocare.ui.activities.profile_screen

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.notifications.BootReceiver
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** [DocumentStreams] sobre o ContentResolver: Drive, Downloads, WhatsApp etc. */
class ContentResolverDocumentStreams(context: Context) : DocumentStreams {
  private val resolver = context.applicationContext.contentResolver

  override fun openInput(uri: Uri): InputStream =
    resolver.openInputStream(uri) ?: throw IOException("Não foi possível abrir o arquivo")

  override fun openOutput(uri: Uri): OutputStream {
    // "wt" trunca um arquivo existente; nem todo provedor aceita o modo, e um
    // documento recem-criado pelo CreateDocument ja esta vazio de qualquer forma.
    val stream = try {
      resolver.openOutputStream(uri, "wt")
    } catch (_: FileNotFoundException) {
      resolver.openOutputStream(uri)
    } catch (_: IllegalArgumentException) {
      resolver.openOutputStream(uri)
    }
    return stream ?: throw IOException("Não foi possível criar o arquivo")
  }

  override fun delete(uri: Uri) {
    runCatching { DocumentsContract.deleteDocument(resolver, uri) }
  }
}

/**
 * Lembretes depois da importacao. Uma manutencao atualizada pode ter mudado de
 * data ou ter sido concluida, entao todo alarme das importadas e cancelado; o
 * worker de reagendamento recria os pendentes e, na mesma passada, recolhe as
 * fotos que ficaram sem dono.
 */
class NotificationImportSideEffects(private val context: Context) : ImportSideEffects {
  override fun onImported(maintenances: List<Maintenance>) {
    maintenances.forEach { NotificationUtils.cancelNotification(context, it) }
    BootReceiver.enqueueReschedule(context)
  }
}

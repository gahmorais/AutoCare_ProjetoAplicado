package br.com.gabrielmorais.autocare.data.backup

import br.com.gabrielmorais.autocare.data.images.ImageKind
import br.com.gabrielmorais.autocare.data.images.ImagePaths
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * [BackupStore] em memoria com a mesma semantica do Room: mescla por id, nada
 * fora do snapshot e apagado, e uma manutencao com id existente muda de
 * veiculo se o arquivo diz isso.
 */
class FakeBackupStore(initial: BackupSnapshot = BackupSnapshot(emptyList(), emptyList())) : BackupStore {
  var current: BackupSnapshot = initial.normalized()
  var mergeFailure: Throwable? = null
  var merges = 0

  override suspend fun snapshot() = current

  override suspend fun merge(snapshot: BackupSnapshot) {
    mergeFailure?.let { throw it }
    merges++

    val profiles = current.profiles.associateBy { it.id } + snapshot.profiles.associateBy { it.id }
    val vehicles = current.vehicles.associateBy { it.id } + snapshot.vehicles.associateBy { it.id }
    val maintenances = maintenanceOwners(current) + maintenanceOwners(snapshot)

    current = BackupSnapshot(
      profiles = profiles.values.toList(),
      vehicles = vehicles.values.map { vehicle ->
        vehicle.copy(maintenances = maintenances.values.filter { it.first == vehicle.id }.map { it.second })
      }
    ).normalized()
  }

  private fun maintenanceOwners(snapshot: BackupSnapshot): Map<Int, Pair<String?, Maintenance>> =
    snapshot.vehicles
      .flatMap { vehicle -> vehicle.maintenances.orEmpty().map { it.id to (vehicle.id to it) } }
      .toMap()
}

/** Ordem estavel, para comparar snapshots com assertEquals. */
fun BackupSnapshot.normalized() = BackupSnapshot(
  profiles = profiles.sortedBy { it.id },
  vehicles = vehicles.sortedBy { it.id }
    .map { it.copy(maintenances = it.maintenances.orEmpty().sortedBy { m -> m.id }) }
)

/** Aceita qualquer JPEG e registra o que conferiu. [failFor] simula uma foto que nao decodifica. */
class FakeImageCheck(private val failFor: Set<String> = emptySet()) : ImportedImageCheck {
  val checked = mutableListOf<Pair<String, ImageKind>>()

  override fun normalize(file: File, kind: ImageKind) {
    val path = "${ImagePaths.DIR}/${file.name}"
    if (path in failFor) throw BackupException.Corrupted("a foto $path não pôde ser aberta")
    checked += path to kind
  }
}

/** JPEG minimo: so o marcador, que e o que o importador confere na JVM. */
fun jpeg(tag: Int = 0) = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), tag.toByte())

fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray {
  val bytes = ByteArrayOutputStream()
  ZipOutputStream(bytes).use { zip ->
    entries.forEach { (name, content) ->
      zip.putNextEntry(ZipEntry(name))
      zip.write(content)
      zip.closeEntry()
    }
  }
  return bytes.toByteArray()
}

fun photoPath(index: Int) = "images/00000000-0000-0000-0000-%012d.jpg".format(index)

fun vehicle(id: String, owner: String, vararg maintenances: Maintenance, photo: String? = null) = Vehicle(
  id = id,
  profileId = owner,
  nickName = id.replaceFirstChar { it.uppercase() },
  photo = photo,
  maintenances = maintenances.toList()
)

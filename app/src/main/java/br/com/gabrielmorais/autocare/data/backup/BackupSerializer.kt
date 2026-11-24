package br.com.gabrielmorais.autocare.data.backup

import br.com.gabrielmorais.autocare.data.images.ImagePaths
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Profile
import br.com.gabrielmorais.autocare.data.models.Vehicle
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

/**
 * manifest.json <-> [BackupSnapshot], mapeado a mao com org.json: nenhuma
 * dependencia nova e nenhuma reflexao, entao o R8 nao tem o que quebrar.
 *
 * Os veiculos vao aninhados no perfil dono e as manutencoes no veiculo: o dono
 * fica implicito na estrutura e nao ha como um arquivo apontar para um perfil
 * inexistente.
 *
 * O arquivo vem de fora e nao e confiavel. [decode] confere tipo e
 * obrigatoriedade de cada campo e falha no primeiro problema, com
 * [BackupException]; nunca devolve um snapshot parcial.
 */
object BackupSerializer {

  fun encode(snapshot: BackupSnapshot, exportedAt: Instant, appVersion: String): String {
    val vehiclesByOwner = snapshot.vehicles.groupBy { it.profileId }
    val profiles = JSONArray()
    snapshot.profiles.forEach { profile ->
      val vehicles = JSONArray()
      vehiclesByOwner[profile.id].orEmpty().forEach { vehicles.put(encodeVehicle(it)) }
      profiles.put(
        JSONObject()
          .put("id", profile.id)
          .put("name", profile.name)
          .putNullable("photoPath", profile.photoPath)
          .put("createdAt", profile.createdAt)
          .put("vehicles", vehicles)
      )
    }
    return JSONObject()
      .put("format", BackupFormat.FORMAT)
      .put("schemaVersion", BackupFormat.SCHEMA_VERSION)
      .put("exportedAt", exportedAt.toString())
      .put("appVersion", appVersion)
      .put("profiles", profiles)
      .toString(2)
  }

  private fun encodeVehicle(vehicle: Vehicle): JSONObject {
    val maintenances = JSONArray()
    vehicle.maintenances.orEmpty().forEach { maintenance ->
      maintenances.put(
        JSONObject()
          .put("id", maintenance.id)
          .putNullable("description", maintenance.description)
          .putNullable("date", maintenance.date)
          .putNullable("currentMileage", maintenance.currentMileage)
          .putNullable("forecastNextExchangeMileage", maintenance.forecastNextExchangeMileage)
          .putNullable("forecastNextExchangeDate", maintenance.forecastNextExchangeDate)
          .putNullable("comments", maintenance.comments)
          .put("completed", maintenance.completed)
      )
    }
    return JSONObject()
      .put("id", requireNotNull(vehicle.id) { "Veículo sem identificador" })
      .putNullable("nickName", vehicle.nickName)
      .putNullable("brand", vehicle.brand)
      .putNullable("model", vehicle.model)
      .putNullable("plate", vehicle.plate)
      .putNullable("photoPath", vehicle.photo)
      .putNullable("averageDistancePerMonth", vehicle.averageDistanceTraveledPerMonth)
      .put("maintenances", maintenances)
  }

  /** put(key, null) do org.json remove a chave; aqui o null fica explicito no arquivo. */
  private fun JSONObject.putNullable(key: String, value: Any?): JSONObject =
    put(key, value ?: JSONObject.NULL)

  /** @throws BackupException se o conteudo nao for um backup valido desta versao. */
  fun decode(json: String): BackupSnapshot {
    val root = try {
      JSONObject(json)
    } catch (error: JSONException) {
      throw BackupException.Corrupted("o conteúdo não pôde ser lido", error)
    }

    if (root.opt("format") != BackupFormat.FORMAT) throw BackupException.NotABackup()
    val version = Reader(root, "arquivo").requiredInt("schemaVersion")
    if (version > BackupFormat.SCHEMA_VERSION) throw BackupException.NewerVersion()
    if (version < 1) throw BackupException.Corrupted("versão de formato inválida ($version)")

    val profiles = mutableListOf<Profile>()
    val vehicles = mutableListOf<Vehicle>()
    val maintenanceIds = mutableSetOf<Int>()

    Reader(root, "arquivo").requiredArray("profiles").forEachObject("perfil") { profileReader ->
      val profileId = profileReader.requiredId("id")
      if (profiles.any { it.id == profileId }) {
        throw BackupException.Corrupted("perfil repetido ($profileId)")
      }
      profiles += Profile(
        id = profileId,
        name = profileReader.requiredString("name").also {
          if (it.isBlank()) throw BackupException.Corrupted("perfil sem nome ($profileId)")
        },
        photoPath = profileReader.optionalPhotoPath("photoPath"),
        createdAt = profileReader.requiredLong("createdAt")
      )

      profileReader.optionalArray("vehicles")?.forEachObject("veículo") { vehicleReader ->
        val vehicleId = vehicleReader.requiredId("id")
        if (vehicles.any { it.id == vehicleId }) {
          throw BackupException.Corrupted("veículo repetido ($vehicleId)")
        }
        val maintenances = mutableListOf<Maintenance>()
        vehicleReader.optionalArray("maintenances")?.forEachObject("manutenção") { reader ->
          val maintenanceId = reader.requiredInt("id")
          if (!maintenanceIds.add(maintenanceId)) {
            throw BackupException.Corrupted("manutenção repetida ($maintenanceId)")
          }
          maintenances += Maintenance(
            id = maintenanceId,
            description = reader.optionalString("description"),
            date = reader.optionalLong("date"),
            currentMileage = reader.optionalInt("currentMileage"),
            forecastNextExchangeMileage = reader.optionalInt("forecastNextExchangeMileage"),
            forecastNextExchangeDate = reader.optionalLong("forecastNextExchangeDate"),
            comments = reader.optionalString("comments"),
            completed = reader.optionalBoolean("completed") ?: false
          )
        }
        vehicles += Vehicle(
          id = vehicleId,
          profileId = profileId,
          nickName = vehicleReader.optionalString("nickName"),
          brand = vehicleReader.optionalString("brand"),
          model = vehicleReader.optionalString("model"),
          plate = vehicleReader.optionalString("plate"),
          photo = vehicleReader.optionalPhotoPath("photoPath"),
          averageDistanceTraveledPerMonth = vehicleReader.optionalInt("averageDistancePerMonth"),
          maintenances = maintenances
        )
      }
    }

    if (profiles.isEmpty()) throw BackupException.Corrupted("nenhum perfil no arquivo")
    return BackupSnapshot(profiles = profiles, vehicles = vehicles)
  }

  /**
   * Leitura tipada de um objeto. `has` + `isNull` em vez de optString/optInt: os
   * opt* do org.json convertem tipo errado em valor default ("" ou 0) e o
   * arquivo invalido passaria calado.
   */
  private class Reader(private val obj: JSONObject, private val where: String) {

    private fun valueOf(key: String): Any? =
      if (!obj.has(key) || obj.isNull(key)) null else obj.get(key)

    private fun wrongType(key: String, expected: String): Nothing =
      throw BackupException.Corrupted("campo \"$key\" de $where não é $expected")

    private fun missing(key: String): Nothing =
      throw BackupException.Corrupted("campo obrigatório \"$key\" ausente em $where")

    fun optionalString(key: String): String? = when (val value = valueOf(key)) {
      null -> null
      is String -> value
      else -> wrongType(key, "texto")
    }

    fun requiredString(key: String): String = optionalString(key) ?: missing(key)

    /**
     * Ids sao texto curto: UUID no app atual. Nao se exige o formato UUID para
     * nao recusar o arquivo de uma versao que use outro esquema de id.
     */
    fun requiredId(key: String): String = requiredString(key).also {
      if (it.isBlank() || it.length > MAX_ID_LENGTH) wrongType(key, "um identificador válido")
    }

    fun optionalPhotoPath(key: String): String? = optionalString(key)?.also {
      // O caminho vai virar arquivo no aparelho: so o formato gerado pelo app.
      if (!ImagePaths.isValid(it)) wrongType(key, "um caminho de foto válido")
    }

    fun optionalLong(key: String): Long? {
      val value = valueOf(key) ?: return null
      return integralOrNull(value) ?: wrongType(key, "um número inteiro")
    }

    fun requiredLong(key: String): Long = optionalLong(key) ?: missing(key)

    fun optionalInt(key: String): Int? {
      val value = optionalLong(key) ?: return null
      if (value !in Int.MIN_VALUE..Int.MAX_VALUE) wrongType(key, "um número inteiro de 32 bits")
      return value.toInt()
    }

    fun requiredInt(key: String): Int = optionalInt(key) ?: missing(key)

    fun optionalBoolean(key: String): Boolean? = when (val value = valueOf(key)) {
      null -> null
      is Boolean -> value
      else -> wrongType(key, "verdadeiro ou falso")
    }

    fun optionalArray(key: String): JSONArray? = when (val value = valueOf(key)) {
      null -> null
      is JSONArray -> value
      else -> wrongType(key, "uma lista")
    }

    fun requiredArray(key: String): JSONArray = optionalArray(key) ?: missing(key)

    /** O org.json devolve Integer, Long, Double ou BigDecimal conforme o texto. */
    private fun integralOrNull(value: Any): Long? = when (value) {
      is Int -> value.toLong()
      is Long -> value
      is BigInteger -> if (value.bitLength() < 64) value.toLong() else null
      is Double -> value.takeIf { it % 1.0 == 0.0 && it in -9.0e15..9.0e15 }?.toLong()
      is BigDecimal -> runCatching { value.longValueExact() }.getOrNull()
      else -> null
    }
  }

  private fun JSONArray.forEachObject(where: String, block: (Reader) -> Unit) {
    for (index in 0 until length()) {
      val item = opt(index) as? JSONObject
        ?: throw BackupException.Corrupted("item ${index + 1} da lista de $where não é um objeto")
      block(Reader(item, where))
    }
  }

  private const val MAX_ID_LENGTH = 100
}

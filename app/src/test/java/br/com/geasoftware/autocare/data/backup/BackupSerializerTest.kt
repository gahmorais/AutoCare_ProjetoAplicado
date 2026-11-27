package br.com.geasoftware.autocare.data.backup

import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.Instant

class BackupSerializerTest {

  private val full = BackupSnapshot(
    profiles = listOf(
      Profile(id = "ana", name = "Ana", photoPath = photoPath(1), createdAt = 1_700_000_000_000L),
      Profile(id = "pedro", name = "Pedro", createdAt = 2L)
    ),
    vehicles = listOf(
      vehicle(
        "gol", "ana",
        Maintenance(
          id = Int.MIN_VALUE,
          description = "Óleo",
          date = -1L,
          currentMileage = 0,
          forecastNextExchangeMileage = Int.MAX_VALUE,
          forecastNextExchangeDate = 20_000L,
          comments = "aspas \" e \\ barra",
          completed = true
        ),
        Maintenance(id = 3),
        photo = photoPath(2)
      ).copy(brand = "VW", model = "Gol", plate = "ABC1D23", averageDistanceTraveledPerMonth = 800),
      vehicle("uno", "pedro").copy(nickName = null)
    )
  )

  private fun decodeFails(json: String): BackupException =
    try {
      BackupSerializer.decode(json)
      fail("deveria recusar: $json")
      throw IllegalStateException()
    } catch (error: BackupException) {
      error
    }

  private fun withProfiles(profiles: String) =
    """{"format":"autocare-backup","schemaVersion":1,"profiles":$profiles}"""

  @Test
  fun encodeThenDecode_keepsEveryFieldIncludingNullsAndLimits() {
    val json = BackupSerializer.encode(full, Instant.parse("2026-11-24T12:00:00Z"), "1.0.0")
    assertEquals(full.normalized(), BackupSerializer.decode(json).normalized())
  }

  @Test
  fun encode_writesTheHeaderAndNestsVehiclesInTheOwner() {
    val root = JSONObject(BackupSerializer.encode(full, Instant.parse("2026-11-24T12:00:00Z"), "1.0.0"))
    assertEquals("autocare-backup", root.getString("format"))
    assertEquals(1, root.getInt("schemaVersion"))
    assertEquals("2026-11-24T12:00:00Z", root.getString("exportedAt"))
    assertEquals("1.0.0", root.getString("appVersion"))
    val ana = root.getJSONArray("profiles").getJSONObject(0)
    assertEquals("gol", ana.getJSONArray("vehicles").getJSONObject(0).getString("id"))
    assertTrue("null fica explicito", ana.getJSONArray("vehicles").getJSONObject(0).has("plate"))
    assertTrue(root.getJSONArray("profiles").getJSONObject(1).isNull("photoPath"))
  }

  @Test
  fun missingCompleted_defaultsToFalse_andMissingListsAreEmpty() {
    val snapshot = BackupSerializer.decode(
      withProfiles(
        """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol","maintenances":[{"id":1}]}]},
            {"id":"bia","name":"Bia","createdAt":2}]"""
      )
    )
    assertFalse(snapshot.vehicles.single().maintenances!!.single().completed)
    assertEquals(2, snapshot.profiles.size)
  }

  @Test
  fun wrongFormat_isNotABackup() {
    assertTrue(decodeFails("""{"format":"outro","schemaVersion":1,"profiles":[]}""") is BackupException.NotABackup)
    assertTrue(decodeFails("""{"schemaVersion":1}""") is BackupException.NotABackup)
  }

  @Test
  fun newerSchema_isRejectedBeforeReadingTheRest() {
    val error = decodeFails("""{"format":"autocare-backup","schemaVersion":2,"profiles":"formato novo"}""")
    assertTrue(error is BackupException.NewerVersion)
  }

  @Test
  fun invalidSchemaVersion_isCorrupted() {
    assertTrue(decodeFails("""{"format":"autocare-backup","schemaVersion":0,"profiles":[]}""") is BackupException.Corrupted)
    assertTrue(decodeFails("""{"format":"autocare-backup","schemaVersion":"1","profiles":[]}""") is BackupException.Corrupted)
  }

  @Test
  fun wrongTypes_areRejectedInsteadOfBecomingDefaults() {
    listOf(
      """[{"id":"ana","name":"Ana","createdAt":"ontem"}]""",
      """[{"id":"ana","name":42,"createdAt":1}]""",
      """[{"id":"ana","name":"Ana","createdAt":1.5}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":{}}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol","averageDistancePerMonth":3000000000}]}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol","maintenances":[{"id":1,"completed":"sim"}]}]}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[7]}]""",
      """["ana"]"""
    ).forEach { profiles ->
      assertTrue(profiles, decodeFails(withProfiles(profiles)) is BackupException.Corrupted)
    }
  }

  @Test
  fun requiredFields_areRequired() {
    listOf(
      """[{"name":"Ana","createdAt":1}]""",
      """[{"id":"ana","createdAt":1}]""",
      """[{"id":"ana","name":"   ","createdAt":1}]""",
      """[{"id":"ana","name":"Ana"}]""",
      """[{"id":"","name":"Ana","createdAt":1}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"nickName":"sem id"}]}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol","maintenances":[{"description":"sem id"}]}]}]""",
      """[]"""
    ).forEach { profiles ->
      assertTrue(profiles, decodeFails(withProfiles(profiles)) is BackupException.Corrupted)
    }
  }

  @Test
  fun photoPathOutsideTheImagesFolder_isRejected() {
    listOf("../../shared_prefs/x.xml", "/data/data/x.jpg", "images/foto.png", "images/../x.jpg").forEach { path ->
      val error = decodeFails(withProfiles("""[{"id":"ana","name":"Ana","createdAt":1,"photoPath":"$path"}]"""))
      assertTrue(path, error is BackupException.Corrupted)
    }
  }

  @Test
  fun repeatedIds_areRejected() {
    listOf(
      """[{"id":"ana","name":"Ana","createdAt":1},{"id":"ana","name":"Ana 2","createdAt":2}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol"},{"id":"gol"}]}]""",
      """[{"id":"ana","name":"Ana","createdAt":1,"vehicles":[{"id":"gol","maintenances":[{"id":1}]},
          {"id":"uno","maintenances":[{"id":1}]}]}]"""
    ).forEach { profiles ->
      assertTrue(profiles, decodeFails(withProfiles(profiles)) is BackupException.Corrupted)
    }
  }

  @Test
  fun notJson_isCorrupted() {
    assertTrue(decodeFails("isto não é json") is BackupException.Corrupted)
    assertTrue(decodeFails("""{"format":"autocare-backup","schemaVersion":1,"profiles":[""") is BackupException.Corrupted)
  }
}

package br.com.geasoftware.autocare.data.images

import br.com.geasoftware.autocare.data.models.Profile
import br.com.geasoftware.autocare.data.models.Vehicle
import br.com.geasoftware.autocare.fakes.FakeProfileRepository
import br.com.geasoftware.autocare.fakes.FakeVehicleRepository
import br.com.geasoftware.autocare.fakes.fakeImageStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoHousekeepingTest {

  @get:Rule
  val tmp = TemporaryFolder()

  @Test
  fun `mantem fotos de perfis e de veiculos e apaga o resto`() = runTest {
    val store = fakeImageStore(tmp.root, UnconfinedTestDispatcher(testScheduler))
    val image = mock(android.net.Uri::class.java)
    val avatar = store.save(image, ImageKind.PROFILE_PHOTO)
    val carPhoto = store.save(image, ImageKind.VEHICLE_PHOTO)
    val replaced = store.save(image, ImageKind.VEHICLE_PHOTO)
    // Todas mais velhas que a carencia, para so a referencia decidir.
    listOf(avatar, carPhoto, replaced).forEach { store.resolve(it).setLastModified(0) }

    val housekeeping = PhotoHousekeeping(
      profiles = FakeProfileRepository(listOf(Profile(id = "ana", name = "Ana", photoPath = avatar))),
      vehicles = FakeVehicleRepository(listOf(Vehicle(id = "gol", profileId = "ana", photo = carPhoto))),
      imageStore = store
    )

    assertEquals(1, housekeeping.sweep())
    assertTrue(store.resolve(avatar).exists())
    assertTrue(store.resolve(carPhoto).exists())
    assertFalse(store.resolve(replaced).exists())
  }
}

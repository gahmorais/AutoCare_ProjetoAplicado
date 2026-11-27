package br.com.geasoftware.autocare.ui.activities.vehicle_details_screen

import android.net.Uri
import br.com.geasoftware.autocare.data.images.ImagePaths
import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
import br.com.geasoftware.autocare.data.models.Vehicle
import br.com.geasoftware.autocare.fakes.FakeMaintenanceRepository
import br.com.geasoftware.autocare.fakes.FakeProfileRepository
import br.com.geasoftware.autocare.fakes.FakeVehicleRepository
import br.com.geasoftware.autocare.fakes.fakeImageStore
import br.com.geasoftware.autocare.fakes.ioFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
internal class VehicleDetailsViewModelTest {

  @get:Rule
  val tmp = TemporaryFolder()

  private val dispatcher = StandardTestDispatcher()
  private val oleo = Maintenance(id = 1, description = "Óleo")
  private lateinit var vehicles: FakeVehicleRepository
  private lateinit var maintenances: FakeMaintenanceRepository
  private val ana = Profile(id = "ana", name = "Ana", createdAt = 1L)
  private val pedro = Profile(id = "pedro", name = "Pedro", createdAt = 2L)
  private val profiles = FakeProfileRepository(listOf(ana, pedro))

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    vehicles = FakeVehicleRepository(
      listOf(Vehicle(id = "gol", profileId = "ana", maintenances = listOf(oleo)))
    )
    maintenances = FakeMaintenanceRepository(vehicles)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun TestScope.viewModel() = VehicleDetailsViewModel(
    vehicleRepository = vehicles,
    maintenanceRepository = maintenances,
    imageStore = fakeImageStore(tmp.root, dispatcher),
    profileRepository = profiles
  ).apply {
    getVehicle("gol")
    advanceUntilIdle()
  }

  @Test
  fun `observa o veiculo pedido`() = runTest(dispatcher) {
    assertEquals("gol", viewModel().vehicle.value?.id)
  }

  @Test
  fun `foto nova grava o caminho local no veiculo`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.uploadVehiclePhoto(Mockito.mock(Uri::class.java))
    advanceUntilIdle()

    assertTrue(ImagePaths.isValid(vm.vehicle.value?.photo.orEmpty()))
    assertEquals("a foto não pode apagar as manutenções", listOf(1), vm.vehicle.value?.maintenances?.map { it.id })
  }

  @Test
  fun `excluir e desfazer manutencao`() = runTest(dispatcher) {
    val vm = viewModel()
    var deleted: Maintenance? = null
    var restored: Maintenance? = null

    vm.deleteMaintenance(oleo) { deleted = it }
    advanceUntilIdle()
    assertEquals(oleo, deleted)
    assertTrue(vm.vehicle.value!!.maintenances!!.isEmpty())

    vm.restoreMaintenance(oleo) { restored = it }
    advanceUntilIdle()
    assertEquals(oleo, restored)
    assertEquals(listOf(oleo), vm.vehicle.value!!.maintenances)
  }

  @Test
  fun `falha ao excluir vira mensagem e nao chama o callback`() = runTest(dispatcher) {
    maintenances.failure = ioFailure("Falha ao gravar")
    val vm = viewModel()
    var deleted: Maintenance? = null

    vm.deleteMaintenance(oleo) { deleted = it }
    advanceUntilIdle()

    assertNull(deleted)
    assertEquals("Falha ao gravar", vm.message.value)
  }

  @Test
  fun `transferencia oferece todos os perfis menos o dono`() = runTest(dispatcher) {
    assertEquals(listOf(pedro), viewModel().transferTargets.value)
  }

  @Test
  fun `transferir muda o dono e mantem as manutencoes`() = runTest(dispatcher) {
    val vm = viewModel()
    var newOwner: Profile? = null

    vm.transferVehicle(pedro) { newOwner = it }
    advanceUntilIdle()

    assertEquals(pedro, newOwner)
    assertEquals("pedro", vm.vehicle.value?.profileId)
    assertEquals(listOf(oleo), vm.vehicle.value?.maintenances)
    assertEquals(listOf(ana), vm.transferTargets.value)
  }
}

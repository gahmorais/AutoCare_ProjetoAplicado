package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Service
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.fakes.FakeMaintenanceRepository
import br.com.gabrielmorais.autocare.fakes.FakeServicesRepository
import br.com.gabrielmorais.autocare.fakes.FakeVehicleRepository
import br.com.gabrielmorais.autocare.fakes.ioFailure
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class AddMaintenanceViewModelTest {

  private val dispatcher = StandardTestDispatcher()
  private val oleo = Service(name = "Óleo de motor", mileageChange = 7_000, mustBeDoneBefore = 6)
  private val existing = Maintenance(id = 7, description = "Óleo de motor")
  private lateinit var vehicles: FakeVehicleRepository
  private lateinit var maintenances: FakeMaintenanceRepository

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    vehicles = FakeVehicleRepository(
      listOf(Vehicle(id = "gol", profileId = "ana", maintenances = listOf(existing)))
    )
    maintenances = FakeMaintenanceRepository(vehicles)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun TestScope.viewModel(services: FakeServicesRepository = FakeServicesRepository(listOf(oleo))) =
    AddMaintenanceViewModel(services, maintenances, vehicles).apply {
      getVehicle("gol")
      advanceUntilIdle()
    }

  @Test
  fun `carrega o catalogo de servicos`() = runTest(dispatcher) {
    val vm = viewModel()

    assertEquals(listOf(oleo), vm.services.value)
    assertFalse(vm.servicesLoading.value)
  }

  @Test
  fun `falha no catalogo encerra o carregamento e vira mensagem`() = runTest(dispatcher) {
    val vm = viewModel(FakeServicesRepository(failure = ioFailure("Catálogo corrompido")))

    assertTrue(vm.services.value.isEmpty())
    assertFalse(vm.servicesLoading.value)
    assertEquals("Catálogo corrompido", vm.message.value)
  }

  @Test
  fun `editar resolve a manutencao pelo id`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.startEditing(7)
    advanceUntilIdle()

    assertEquals(existing, vm.editingMaintenance.value)
  }

  @Test
  fun `salvar grava e so entao chama onSaved`() = runTest(dispatcher) {
    val vm = viewModel()
    var saved = false
    val nova = Maintenance(id = 8, description = "Velas")

    vm.saveMaintenance("gol", nova) { saved = true }
    advanceUntilIdle()

    assertTrue(saved)
    assertEquals(setOf(7, 8), vm.vehicle.value!!.maintenances!!.map { it.id }.toSet())
  }

  @Test
  fun `editar substitui pelo mesmo id em vez de duplicar`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.saveMaintenance("gol", existing.copy(comments = "5W30")) {}
    advanceUntilIdle()

    assertEquals("5W30", vm.vehicle.value!!.maintenances!!.single().comments)
  }

  @Test
  fun `falha ao salvar nao chama onSaved`() = runTest(dispatcher) {
    maintenances.failure = ioFailure("Disco cheio")
    val vm = viewModel()
    var saved = false

    vm.saveMaintenance("gol", Maintenance(id = 9)) { saved = true }
    advanceUntilIdle()

    assertFalse("a notificação não pode ser agendada sem a gravação", saved)
    assertEquals("Disco cheio", vm.message.value)
  }
}

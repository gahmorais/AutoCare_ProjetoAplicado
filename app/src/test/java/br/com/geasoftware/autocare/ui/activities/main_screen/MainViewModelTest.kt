package br.com.geasoftware.autocare.ui.activities.main_screen

import android.net.Uri
import br.com.geasoftware.autocare.data.images.ImagePaths
import br.com.geasoftware.autocare.data.models.Maintenance
import br.com.geasoftware.autocare.data.models.Profile
import br.com.geasoftware.autocare.data.models.Vehicle
import br.com.geasoftware.autocare.fakes.FakeActiveProfileStore
import br.com.geasoftware.autocare.fakes.FakeProfileRepository
import br.com.geasoftware.autocare.fakes.FakeVehicleRepository
import br.com.geasoftware.autocare.fakes.fakeImageStore
import br.com.geasoftware.autocare.fakes.ioFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
internal class MainViewModelTest {

  @get:Rule
  val tmp = TemporaryFolder()

  private val dispatcher = StandardTestDispatcher()
  private val ana = Profile(id = "ana", name = "Ana", createdAt = 1L)
  private val pedro = Profile(id = "pedro", name = "Pedro", createdAt = 2L)

  private lateinit var profiles: FakeProfileRepository
  private lateinit var vehicles: FakeVehicleRepository
  private lateinit var activeStore: FakeActiveProfileStore
  private var photoFailure: Throwable? = null

  // O fake de compressao nunca le o conteudo: basta uma referencia.
  private val anyImage: Uri = Mockito.mock(Uri::class.java)

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    profiles = FakeProfileRepository()
    vehicles = FakeVehicleRepository()
    activeStore = FakeActiveProfileStore()
    photoFailure = null
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun TestScope.viewModel(): MainViewModel = MainViewModel(
    profileRepository = profiles,
    vehicleRepository = vehicles,
    activeProfileStore = activeStore,
    imageStore = fakeImageStore(tmp.root, dispatcher) { photoFailure }
  ).also { advanceUntilIdle() }

  private suspend fun MainViewModel.lastMessage() = message.first()

  @Test
  fun `sem perfil o app pede o primeiro acesso`() = runTest(dispatcher) {
    assertEquals(StartState.NEEDS_ONBOARDING, viewModel().startState.value)
  }

  @Test
  fun `com perfil o app abre direto e o perfil fica ativo`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    activeStore.activeProfileId.value = "ana"

    val vm = viewModel()

    assertEquals(StartState.READY, vm.startState.value)
    assertEquals(ana, vm.activeProfile.value)
  }

  @Test
  fun `restaurar um arquivo no primeiro acesso libera o app sem criar perfil`() = runTest(dispatcher) {
    // A importacao grava os perfis direto no banco, sem passar pelo createProfile.
    val vm = viewModel()
    assertEquals(StartState.NEEDS_ONBOARDING, vm.startState.value)

    profiles.profiles.value = listOf(ana, pedro)
    advanceUntilIdle()

    assertEquals(StartState.READY, vm.startState.value)
    assertEquals(ana, vm.activeProfile.value)
    assertEquals(2, vm.profiles.value.size)
  }

  @Test
  fun `id ativo de perfil que nao existe cai no primeiro perfil`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "excluido"

    assertEquals(ana, viewModel().activeProfile.value)
  }

  @Test
  fun `primeiro acesso cria o perfil, ativa e libera o app`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.createProfile("  Maria  ")
    advanceUntilIdle()

    val created = profiles.profiles.value.single()
    assertEquals("Maria", created.name)
    assertNull(created.photoPath)
    assertEquals(created.id, activeStore.activeProfileId.value)
    assertEquals(StartState.READY, vm.startState.value)
  }

  @Test
  fun `primeiro acesso com foto guarda o caminho local`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.createProfile("Maria", anyImage)
    advanceUntilIdle()

    val path = profiles.profiles.value.single().photoPath
    assertTrue("caminho inválido: $path", path != null && ImagePaths.isValid(path))
  }

  @Test
  fun `foto que falha nao impede o primeiro acesso`() = runTest(dispatcher) {
    photoFailure = ioFailure("Não foi possível ler a imagem selecionada")
    val vm = viewModel()

    vm.createProfile("Maria", anyImage)
    advanceUntilIdle()

    assertNull(profiles.profiles.value.single().photoPath)
    assertEquals("Não foi possível ler a imagem selecionada", vm.lastMessage())
  }

  @Test
  fun `nome em branco nao cria perfil`() = runTest(dispatcher) {
    val vm = viewModel()

    vm.createProfile("   ")
    advanceUntilIdle()

    assertTrue(profiles.profiles.value.isEmpty())
    assertEquals("Informe seu nome", vm.lastMessage())
  }

  @Test
  fun `lista so os veiculos do perfil ativo`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "pedro"
    vehicles.vehicles.value = listOf(
      Vehicle(id = "gol", profileId = "ana"),
      Vehicle(id = "uno", profileId = "pedro")
    )

    assertEquals(listOf("uno"), viewModel().vehicles.value.map { it.id })
  }

  @Test
  fun `veiculo novo fica com o perfil ativo como dono`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "pedro"
    val vm = viewModel()

    vm.saveVehicle(Vehicle(id = "uno", nickName = "Uno", profileId = "outro"))
    advanceUntilIdle()

    assertEquals("pedro", vehicles.vehicles.value.single().profileId)
    assertEquals(listOf("uno"), vm.vehicles.value.map { it.id })
  }

  @Test
  fun `falha ao salvar veiculo vira mensagem`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    vehicles.failure = ioFailure("Disco cheio")
    val vm = viewModel()

    vm.saveVehicle(Vehicle(id = "gol"))
    advanceUntilIdle()

    assertEquals("Disco cheio", vm.lastMessage())
  }

  @Test
  fun `excluir e desfazer devolvem o veiculo com as manutencoes`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val gol = Vehicle(id = "gol", profileId = "ana", maintenances = listOf(Maintenance(id = 1)))
    vehicles.vehicles.value = listOf(gol)
    val vm = viewModel()
    var deleted: Vehicle? = null

    vm.deleteVehicle(gol) { deleted = it }
    advanceUntilIdle()
    assertEquals(gol, deleted)
    assertTrue(vm.vehicles.value.isEmpty())

    vm.restoreVehicle(deleted!!)
    advanceUntilIdle()
    assertEquals(listOf(1), vm.vehicles.value.single().maintenances!!.map { it.id })
  }

  @Test
  fun `atualiza o nome do perfil sem espacos nas pontas`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val vm = viewModel()

    vm.updateProfileName("  Ana Maria ")
    advanceUntilIdle()

    assertEquals("Ana Maria", vm.activeProfile.value?.name)
  }

  @Test
  fun `troca a foto do perfil`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val vm = viewModel()

    vm.updateProfilePhoto(anyImage)
    advanceUntilIdle()

    assertTrue(ImagePaths.isValid(vm.activeProfile.value?.photoPath.orEmpty()))
  }

  @Test
  fun `falha na foto do perfil vira mensagem e mantem a foto anterior`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana.copy(photoPath = "images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg"))
    photoFailure = ioFailure("O arquivo selecionado não é uma imagem válida")
    val vm = viewModel()

    vm.updateProfilePhoto(anyImage)
    advanceUntilIdle()

    assertEquals("images/0b6f6a3e-2a7d-4d3b-9a43-1f3d7c1b2e4a.jpg", vm.activeProfile.value?.photoPath)
    assertEquals("O arquivo selecionado não é uma imagem válida", vm.lastMessage())
  }

  // --- Multiplos perfis (etapa 6) ---

  private fun TestScope.twoProfilesWithCars(): MainViewModel {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "ana"
    vehicles.vehicles.value = listOf(
      Vehicle(id = "gol", profileId = "ana", maintenances = listOf(Maintenance(id = 1), Maintenance(id = 2))),
      Vehicle(id = "uno", profileId = "pedro", maintenances = listOf(Maintenance(id = 3)))
    )
    return viewModel()
  }

  @Test
  fun `adicionar veiculo com o inicio filtrado em outro perfil cria nele`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "ana"
    val vm = viewModel()
    vm.setHomeFilter("pedro")

    vm.prepareAddVehicle()
    advanceUntilIdle()
    vm.saveVehicle(Vehicle(id = "uno", nickName = "Uno"))
    advanceUntilIdle()

    assertEquals("pedro", vm.activeProfile.value?.id)
    assertEquals("pedro", vehicles.vehicles.value.single().profileId)
  }

  @Test
  fun `adicionar veiculo sem filtro ou com o filtro no ativo nao troca o perfil`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana, pedro)
    activeStore.activeProfileId.value = "ana"
    val vm = viewModel()

    vm.prepareAddVehicle()
    advanceUntilIdle()
    assertEquals("ana", vm.activeProfile.value?.id)

    vm.setHomeFilter("ana")
    vm.prepareAddVehicle()
    advanceUntilIdle()
    assertEquals("ana", vm.activeProfile.value?.id)
  }

  @Test
  fun `trocar o perfil ativo troca a lista de veiculos`() = runTest(dispatcher) {
    val vm = twoProfilesWithCars()
    assertEquals(listOf("gol"), vm.vehicles.value.map { it.id })

    vm.selectProfile("pedro")
    advanceUntilIdle()

    assertEquals("pedro", vm.activeProfile.value?.id)
    assertEquals(listOf("uno"), vm.vehicles.value.map { it.id })
  }

  @Test
  fun `inicio mostra todos os perfis e o filtro restringe a um`() = runTest(dispatcher) {
    val vm = twoProfilesWithCars()
    assertEquals(setOf("gol", "uno"), vm.homeVehicles.value.map { it.id }.toSet())

    vm.setHomeFilter("pedro")
    advanceUntilIdle()
    assertEquals(listOf("uno"), vm.homeVehicles.value.map { it.id })

    vm.setHomeFilter(null)
    advanceUntilIdle()
    assertEquals(2, vm.homeVehicles.value.size)
  }

  @Test
  fun `nome do dono so entra com mais de um perfil`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val vm = viewModel()
    assertTrue(vm.ownerNames.value.isEmpty())

    profiles.profiles.value = listOf(ana, pedro)
    advanceUntilIdle()
    assertEquals(mapOf("ana" to "Ana", "pedro" to "Pedro"), vm.ownerNames.value)
  }

  @Test
  fun `perfil novo vira o ativo`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val vm = viewModel()

    vm.createProfile("Pedro")
    advanceUntilIdle()

    assertEquals(2, vm.profiles.value.size)
    assertEquals("Pedro", vm.activeProfile.value?.name)
  }

  @Test
  fun `o ultimo perfil nao pode ser excluido`() = runTest(dispatcher) {
    profiles.profiles.value = listOf(ana)
    val vm = viewModel()

    vm.deleteProfile("ana")
    advanceUntilIdle()

    assertEquals(listOf(ana), profiles.profiles.value)
    assertEquals("É preciso manter ao menos um perfil", vm.lastMessage())
  }

  @Test
  fun `excluir o perfil ativo ativa outro e devolve os veiculos removidos`() = runTest(dispatcher) {
    val vm = twoProfilesWithCars()
    var removed: List<Vehicle>? = null

    vm.deleteProfile("ana") { removed = it }
    advanceUntilIdle()

    assertEquals(listOf("pedro"), vm.profiles.value.map { it.id })
    assertEquals("pedro", activeStore.activeProfileId.value)
    assertEquals("pedro", vm.activeProfile.value?.id)
    // Os veiculos removidos vem com as manutencoes, para cancelar os alarmes.
    assertEquals(listOf(1, 2), removed!!.single().maintenances!!.map { it.id })
  }

  @Test
  fun `excluir o perfil filtrado no inicio volta o filtro para todos`() = runTest(dispatcher) {
    val vm = twoProfilesWithCars()
    vm.setHomeFilter("pedro")

    vm.deleteProfile("pedro")
    advanceUntilIdle()

    assertNull(vm.homeFilter.value)
    assertEquals("ana", vm.activeProfile.value?.id)
  }
}

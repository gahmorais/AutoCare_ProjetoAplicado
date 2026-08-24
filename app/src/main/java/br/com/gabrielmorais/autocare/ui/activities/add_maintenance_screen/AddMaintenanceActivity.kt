package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.ui.components.LoadingPage
import br.com.gabrielmorais.autocare.ui.components.SelectMenu
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants
import br.com.gabrielmorais.autocare.utils.Utils
import br.com.gabrielmorais.autocare.utils.getParcelableExtraCompat
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddMaintenanceActivity : ComponentActivity() {
  private val viewModel: AddMaintenanceViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme() {
        AddMaintenanceScreen(viewModel)
      }
    }

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        message?.let { Toast.makeText(this@AddMaintenanceActivity, it, Toast.LENGTH_SHORT).show() }
      }
    }

  }

  override fun onStart() {
    super.onStart()
    val extras = intent.extras
    extras?.let { bundle ->
      val vehicleId = bundle.getString(Constants.INTENT_VEHICLE_ID)
      if (vehicleId != null) {
        viewModel.getVehicle(vehicleId)
      }
      // Extra ausente significa modo de criacao.
      intent.getParcelableExtraCompat<Maintenance>(Constants.INTENT_MAINTENANCE)
        ?.let(viewModel::startEditing)
    }
  }
}

/**
 * Servico ja validado: os campos vindos do Firebase sao nulaveis e um registro
 * incompleto derrubava a tela inteira.
 */
private data class ServiceOption(
  val name: String,
  val mileageChange: Int,
  val mustBeDoneBefore: Int
)

@Composable
fun AddMaintenanceScreen(viewModel: AddMaintenanceViewModel) {
  val services by viewModel.services.collectAsState()
  val servicesLoading by viewModel.servicesLoading.collectAsState()
  val userId by viewModel.userId.collectAsState()
  val vehicle by viewModel.vehicle.collectAsState()
  val editing by viewModel.editingMaintenance.collectAsState()
  val context = LocalContext.current as ComponentActivity

  // Precisa de remember: sem ele o estado era recriado a cada recomposicao,
  // descartando tudo que o usuario digitasse. A chave e o id da manutencao
  // porque em modo edicao ela chega depois da primeira composicao, e o estado
  // precisa ser semeado uma unica vez quando isso acontece.
  val state = remember(editing?.id) { AddMaintenanceUiState(editing) }

  val options = remember(services) {
    services.mapNotNull { service ->
      val name = service?.name
      val mileageChange = service?.mileageChange
      val mustBeDoneBefore = service?.mustBeDoneBefore
      if (name != null && mileageChange != null && mustBeDoneBefore != null) {
        ServiceOption(name, mileageChange, mustBeDoneBefore)
      } else {
        null
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(
              if (editing != null) R.string.text_edit_maintenance else R.string.add_maintenance
            )
          )
        }
      )
    }
  ) { paddingValues ->
    Column(
      Modifier
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
      Spacer(modifier = Modifier.padding(top = 20.dp))

      // Nada de `return@Column` aqui. Sair antes do fim de uma lambda de
      // composable deixa o Composer com grupos abertos sem o endNode
      // correspondente, e a composicao morre com IndexOutOfBoundsException em
      // ComposerImpl.endNode. Os tres estados sao ramos de um when.
      when {
        // Antes a tela inteira ficava em branco enquanto a lista nao chegava, e
        // permanecia assim se a leitura falhasse.
        servicesLoading -> LoadingPage(stringResource(R.string.text_loading_services))

        options.isEmpty() -> Text(
          text = stringResource(R.string.text_services_unavailable),
          style = Typography.h6
        )

        // Column propria porque o formulario cresceu com o switch e a barra de
        // titulo, e sem rolagem o botao de salvar sai da tela em aparelhos
        // menores. A rolagem fica so neste ramo: LoadingPage usa fillMaxSize,
        // que dentro de um verticalScroll recebe altura infinita e para de
        // centralizar.
        else -> Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
          MaintenanceForm(
            state = state,
            options = options,
            vehicle = vehicle,
            editing = editing,
            canSave = userId.isNotBlank(),
            onSave = { vehicleId, maintenance ->
              // Retorno antecipado aqui e seguro: onSave e lambda de evento.
              val onSaved: () -> Unit = {
                NotificationUtils.rescheduleNotification(
                  context = context,
                  maintenance = maintenance,
                  localDateTime = Utils.dateMinusFiveDays(state.forecastNextExchangeDate)
                )
                context.finish()
              }

              if (editing != null) {
                viewModel.updateMaintenance(
                  vehicleId = vehicleId,
                  maintenance = maintenance,
                  onSaved = onSaved
                )
              } else {
                val current = vehicle ?: return@MaintenanceForm
                viewModel.saveMaintenance(
                  userId = userId,
                  vehicleId = vehicleId,
                  updatedVehicle = current.copy(
                    maintenances = current.maintenances.orEmpty() + maintenance
                  ),
                  onSaved = onSaved
                )
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun MaintenanceForm(
  state: AddMaintenanceUiState,
  options: List<ServiceOption>,
  vehicle: Vehicle?,
  editing: Maintenance?,
  canSave: Boolean,
  onSave: (vehicleId: String, maintenance: Maintenance) -> Unit
) {
  val isEditing = editing != null
  var expanded by remember { mutableStateOf(false) }
  var selectedIndex by remember(options, editing?.id) {
    // Em modo edicao comeca no servico do registro; caso nao esteja mais na
    // lista, cai no primeiro.
    mutableStateOf(options.indexOfFirst { it.name == editing?.description }.coerceAtLeast(0))
  }
  val selectedOption = options[selectedIndex.coerceIn(options.indices)]
  val averageTraveledDistance = vehicle?.averageDistanceTraveledPerMonth

  val datepickerDialog = rememberMaterialDialogState()
  val datepickerNextMaintenance = rememberMaterialDialogState()

  MaterialDialog(
    dialogState = datepickerDialog,
    buttons = {
      positiveButton("OK")
      negativeButton("Cancel")
    }
  ) {
    datepicker { state.onDateChange(it) }
  }

  MaterialDialog(
    dialogState = datepickerNextMaintenance,
    buttons = {
      positiveButton("OK")
      negativeButton("Cancel")
    }
  ) {
    datepicker { state.onForecastNextExchangeDateChange(it) }
  }

  // Ao abrir uma manutencao existente, os dois calculos derivados abaixo
  // disparariam de imediato e sobrescreveriam a previsao ja gravada. Cada um
  // pula a primeira execucao em modo edicao; alteracoes seguintes do usuario
  // voltam a recalcular normalmente.
  var mileageDerivationArmed by remember(editing?.id) { mutableStateOf(!isEditing) }
  var dateDerivationArmed by remember(editing?.id) { mutableStateOf(!isEditing) }

  // Escrever estado durante a composicao realimenta a recomposicao; por isso
  // os dois calculos derivados moram em LaunchedEffect.
  LaunchedEffect(selectedOption, state.currentMileage) {
    if (!mileageDerivationArmed) {
      mileageDerivationArmed = true
      return@LaunchedEffect
    }
    val currentMileage = state.currentMileage.toIntOrNull() ?: 0
    state.onForecastNextExchangeMileageChange(
      (selectedOption.mileageChange + currentMileage).toString()
    )
  }

  LaunchedEffect(selectedOption, state.date, averageTraveledDistance) {
    if (!dateDerivationArmed) {
      dateDerivationArmed = true
      return@LaunchedEffect
    }
    // Sem a guarda, media nula estourava NPE e media zero, divisao por zero.
    val monthsByMileage = if (averageTraveledDistance != null && averageTraveledDistance > 0) {
      selectedOption.mileageChange / averageTraveledDistance
    } else {
      0
    }
    val months = maxOf(monthsByMileage, selectedOption.mustBeDoneBefore)
    Utils.futureDateMonth(state.date, months)
      ?.let { state.onForecastNextExchangeDateChange(it) }
  }

  SelectMenu(
    modifier = Modifier.fillMaxWidth(),
    items = options.map { it.name },
    expanded = expanded,
    onExpandedChange = { expanded = !expanded },
    value = selectedOption.name,
    label = stringResource(R.string.text_service_type),
    onDissmis = { expanded = false },
    onClick = { _, index ->
      selectedIndex = index
      expanded = false
    }
  )

  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = Utils.formatDate(state.date.toEpochDay()),
    label = { Text(stringResource(id = R.string.text_date)) },
    readOnly = true,
    trailingIcon = {
      IconButton(onClick = { datepickerDialog.show() }) {
        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
      }
    },
    onValueChange = {}
  )

  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = state.currentMileage,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    label = { Text(stringResource(id = R.string.text_current_mileage)) },
    onValueChange = { state.onCurrentMilageChange(it.filter(Char::isDigit)) }
  )

  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = state.forecastNextExchangeMileage,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    label = { Text(stringResource(id = R.string.text_next_maintenance_mileage)) },
    onValueChange = { state.onForecastNextExchangeMileageChange(it.filter(Char::isDigit)) }
  )

  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = Utils.formatDate(state.forecastNextExchangeDate.toEpochDay()),
    label = { Text(stringResource(id = R.string.text_next_date_maintenance)) },
    readOnly = true,
    trailingIcon = {
      IconButton(onClick = { datepickerNextMaintenance.show() }) {
        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
      }
    },
    onValueChange = {}
  )

  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = state.comments,
    label = { Text(stringResource(id = R.string.text_comments)) },
    onValueChange = state.onCommentsChange
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = stringResource(R.string.text_mark_as_executed))
    Switch(
      checked = state.completed,
      onCheckedChange = state.onCompletedChange
    )
  }

  val currentMileage = state.currentMileage.toIntOrNull()

  OutlinedButton(
    modifier = Modifier.fillMaxWidth(),
    enabled = canSave && vehicle?.id != null && currentMileage != null,
    onClick = {
      // Retorno antecipado aqui e seguro: onClick e lambda de evento, nao de
      // composicao.
      val vehicleId = vehicle?.id ?: return@OutlinedButton
      val mileage = currentMileage ?: return@OutlinedButton
      val nextMileage = state.forecastNextExchangeMileage.toIntOrNull() ?: mileage

      // Partir de `editing` preserva o id, que e tanto a chave do update quanto
      // o requestCode do alarme; em modo de criacao Maintenance() sorteia um.
      val maintenance = (editing ?: Maintenance()).copy(
        description = selectedOption.name,
        date = state.date.toEpochDay(),
        currentMileage = mileage,
        forecastNextExchangeMileage = nextMileage,
        forecastNextExchangeDate = state.forecastNextExchangeDate.toEpochDay(),
        comments = state.comments,
        completed = state.completed
      )

      onSave(vehicleId, maintenance)
    }) {
    Text(text = stringResource(R.string.text_save), style = Typography.h5)
  }
}

@Preview(showBackground = true)
@Composable
fun AddMaintenanceScreenPreview() {
//  AddMaintenanceScreen()
}

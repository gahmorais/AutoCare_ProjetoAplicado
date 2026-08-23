package br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.ui.components.SelectMenu
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants
import br.com.gabrielmorais.autocare.utils.Utils
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
      val userId = bundle.getString(Constants.INTENT_USER_ID)
      val vehicleId = bundle.getString(Constants.INTENT_VEHICLE_ID)
      if (userId != null && vehicleId != null) {
        viewModel.setUserId(userId)
        viewModel.getVehicle(userId, vehicleId)
      }
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
  // Precisa de remember: sem ele o estado era recriado a cada recomposicao,
  // descartando tudo que o usuario digitasse.
  val state = remember { AddMaintenanceUiState() }
  val services by viewModel.services.collectAsState()
  val userId by viewModel.userId.collectAsState()
  val vehicle by viewModel.vehicle.collectAsState()
  val context = LocalContext.current as ComponentActivity

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

  Scaffold { paddingValues ->
    Column(
      Modifier
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
      Spacer(modifier = Modifier.padding(top = 20.dp))

      if (options.isEmpty()) {
        Text(
          text = stringResource(R.string.text_services_unavailable),
          style = Typography.h6
        )
        return@Column
      }

      var expanded by remember { mutableStateOf(false) }
      var selectedIndex by remember(options) { mutableStateOf(0) }
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
        datepicker() {
          state.onDateChange(it)
        }
      }

      MaterialDialog(
        dialogState = datepickerNextMaintenance,
        buttons = {
          positiveButton("OK")
          negativeButton("Cancel")
        }
      ) {
        datepicker {
          state.onForecastNextExchangeDateChange(it)
        }
      }

      // Escrever estado durante a composicao realimenta a recomposicao; por isso
      // os dois calculos derivados moram em LaunchedEffect.
      LaunchedEffect(selectedOption, state.currentMileage) {
        val currentMileage = state.currentMileage.toIntOrNull() ?: 0
        state.onForecastNextExchangeMileageChange(
          (selectedOption.mileageChange + currentMileage).toString()
        )
      }

      LaunchedEffect(selectedOption, state.date, averageTraveledDistance) {
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

      val currentVehicle = vehicle
      val currentMileage = state.currentMileage.toIntOrNull()

      OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = currentVehicle?.id != null && currentMileage != null && userId.isNotBlank(),
        onClick = {
          val vehicleId = currentVehicle?.id ?: return@OutlinedButton
          val mileage = currentMileage ?: return@OutlinedButton
          val nextMileage = state.forecastNextExchangeMileage.toIntOrNull() ?: mileage

          val maintenances = currentVehicle.maintenances.orEmpty().toMutableList()

          val maintenance = Maintenance(
            description = selectedOption.name,
            date = state.date.toEpochDay(),
            currentMileage = mileage,
            forecastNextExchangeMileage = nextMileage,
            forecastNextExchangeDate = state.forecastNextExchangeDate.toEpochDay(),
            comments = state.comments
          )

          maintenances.add(maintenance)

          viewModel.saveMaintenance(
            userId = userId,
            vehicleId = vehicleId,
            updatedVehicle = currentVehicle.copy(maintenances = maintenances),
            onSaved = {
              NotificationUtils.scheduleNotification(
                context = context,
                maintenance = maintenance,
                localDateTime = Utils.dateMinusFiveDays(state.forecastNextExchangeDate)
              )
              context.finish()
            }
          )
        }) {
        Text(text = stringResource(R.string.text_save), style = Typography.h5)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AddMaintenanceScreenPreview() {
//  AddMaintenanceScreen()
}

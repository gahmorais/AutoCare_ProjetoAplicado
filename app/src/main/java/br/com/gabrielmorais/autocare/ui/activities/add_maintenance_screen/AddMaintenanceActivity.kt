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


@Composable
fun AddMaintenanceScreen(viewModel: AddMaintenanceViewModel) {
  val state = AddMaintenanceUiState()
  val services = viewModel.services.collectAsState()
  val userId = viewModel.userId.collectAsState()
  val vehicle = viewModel.vehicle.collectAsState()
  val context = LocalContext.current as ComponentActivity
//  val applicationContext = LocalContext.current.applicationContext
  Scaffold { paddingValues ->
    Column(
      Modifier
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
      Spacer(modifier = Modifier.padding(top = 20.dp))
      if (services.value.isNotEmpty()) {
        val servicesName = services.value.map { it?.name!! }
        val servicesMileage = services.value.map { it?.mileageChange!! }
        val servicesTime = services.value.map { it?.mustBeDoneBefore!! }
        var expanded by remember { mutableStateOf(false) }
        var selectItem by remember { mutableStateOf(servicesName[0]) }
        var serviceSelected by remember { mutableStateOf(servicesMileage[0]) }
        val averageTraveledDistance = vehicle.value?.averageDistanceTraveledPerMonth

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

        if (state.currentMileage.isEmpty()) {
          state.onForecastNextExchangeMileageChange(serviceSelected.toString())
        } else {
          val mileageToNextMaintenance = serviceSelected + state.currentMileage.toInt()
          state.onForecastNextExchangeMileageChange(mileageToNextMaintenance.toString())
        }

        SelectMenu(
          modifier = Modifier.fillMaxWidth(),
          items = servicesName,
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
          value = selectItem,
          label = stringResource(R.string.text_service_type),
          onDissmis = { expanded = false },
          onClick = { itemSelected, i ->
            selectItem = itemSelected
            serviceSelected = servicesMileage[i]
            expanded = false
            val monthsToNextMaintenance = serviceSelected.div(averageTraveledDistance!!)
            val months = if (monthsToNextMaintenance > servicesTime[i]) {
              monthsToNextMaintenance
            } else {
              servicesTime[i]
            }

            val dateNextMaintenance = Utils.futureDateMonth(state.date, months)
            dateNextMaintenance?.let { state.onForecastNextExchangeDateChange(it) }
          }
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = Utils.formatDate(state.date.toEpochDay()),
          label = { Text(stringResource(id = R.string.text_date)) },
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
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          label = { Text(stringResource(id = R.string.text_current_mileage)) },
          onValueChange = state.onCurrentMilageChange
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = state.forecastNextExchangeMileage,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          label = { Text(stringResource(id = R.string.text_next_maintenance_mileage)) },
          onValueChange = state.onForecastNextExchangeMileageChange
        )

        OutlinedTextField(
          modifier = Modifier.fillMaxWidth(),
          value = Utils.formatDate(state.forecastNextExchangeDate.toEpochDay()),
          label = { Text(stringResource(id = R.string.text_next_date_maintenance)) },
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

        OutlinedButton(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            val maintenances = vehicle
              .value
              ?.maintenances
              ?.toMutableList() ?: mutableListOf()

            val maintenance = Maintenance(
              description = selectItem,
              date = state.date.toEpochDay(),
              currentMileage = state.currentMileage.toInt(),
              forecastNextExchangeMileage = state.forecastNextExchangeMileage.toInt(),
              forecastNextExchangeDate = state.forecastNextExchangeDate.toEpochDay(),
              comments = state.comments
            )

            maintenances.add(maintenance)
            val updatedVehicle = vehicle.value?.copy(maintenances = maintenances)

            viewModel.saveMaintenance(
              userId = userId.value,
              vehicleId = updatedVehicle?.id!!,
              updatedVehicle = updatedVehicle,
            )

            NotificationUtils.scheduleNotification(
              context = context,
              maintenance = maintenance,
              localDateTime = Utils.dateMinusFiveDays(state.forecastNextExchangeDate)
            )

            context.finish()
          }) {
          Text(text = "Gravar", style = Typography.h5)
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun AddMaintenanceScreenPreview() {
//  AddMaintenanceScreen()
}
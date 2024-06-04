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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants
import br.com.gabrielmorais.autocare.utils.Utils
import br.com.gabrielmorais.autocare.utils.findActivity
import com.canhub.cropper.parcelable
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.time.LocalDate

class AddMaintenanceActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        AddMaintenanceScreen()
      }
    }
  }
}


@Composable
fun AddMaintenanceScreen(viewModel: AddMaintenanceViewModel = koinViewModel()) {
  val state = AddMaintenanceUiState()
  val context = LocalContext.current as ComponentActivity
  val message by viewModel.message.collectAsState()
  val scope = rememberCoroutineScope()

  var vehicleId by remember {
    mutableStateOf("")
  }

  var maintenanceId by remember {
    mutableStateOf("")
  }

  LaunchedEffect(message) {
    if (message != null) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
  }

  val activity = context.findActivity()
  val intent = activity?.intent

  LaunchedEffect(key1 = intent?.getStringExtra(Constants.INTENT_VEHICLE_ID)) {
    val bundle = intent?.getStringExtra(Constants.INTENT_VEHICLE_ID)
    if (bundle != null) {
      vehicleId = bundle
      Timber.tag("MainActivity").i("User Id: $vehicleId")
    }
  }

  LaunchedEffect(key1 = intent?.getBundleExtra(activity.getString(R.string.edit_maintenance_key))) {
    val maintenance = intent
      ?.parcelable<Maintenance>(activity.getString(R.string.edit_maintenance_key))
    if (maintenance != null) {
      maintenanceId = maintenance.id
      vehicleId = maintenance.vehicleId
      state.onCommentsChange(maintenance.comments ?: "")
      state.onCurrentMilageChange(maintenance.currentMileage.toString())
      state.onForecastNextExchangeMileageChange(maintenance.forecastNextExchangeMileage.toString())
      state.onServiceChange(maintenance.description ?: "")
      val date = LocalDate.ofEpochDay(maintenance.date ?: 0)
      val nextDate = LocalDate.ofEpochDay(maintenance.forecastNextExchangeDate ?: 0)
      state.onDateChange(date)
      state.onForecastNextExchangeDateChange(nextDate)
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

      val datepickerDialog = rememberMaterialDialogState()
      val datepickerNextMaintenance = rememberMaterialDialogState()

      val timePicker = rememberMaterialDialogState()
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

      OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.service,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        label = { Text(stringResource(id = R.string.text_service_type)) },
        onValueChange = state.onServiceChange
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

          var newMaintenance = Maintenance(
            vehicleId = vehicleId,
            description = state.service,
            date = state.date.toEpochDay(),
            currentMileage = state.currentMileage.toInt(),
            forecastNextExchangeMileage = state.forecastNextExchangeMileage.toInt(),
            forecastNextExchangeDate = state.forecastNextExchangeDate.toEpochDay(),
            comments = state.comments
          )

          if (maintenanceId.isNotEmpty()) {
            newMaintenance = newMaintenance.copy(id = maintenanceId)
          }

          scope.launch { viewModel.saveMaintenance(newMaintenance) }

          context.finish()
        }) {
        Text(text = "Gravar", style = Typography.h5)
      }
    }

  }
}

@Preview(showBackground = true)
@Composable
fun AddMaintenanceScreenPreview() {
  AddMaintenanceScreen()
}
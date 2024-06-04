package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen.AddMaintenanceActivity
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import br.com.gabrielmorais.autocare.ui.components.CardVehicleDetails
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_VEHICLE_ID
import br.com.gabrielmorais.autocare.utils.findActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class VehicleDetailsActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        VehicleDetailsScreen()
      }
    }
  }
}

@Composable
fun VehicleDetailsScreen(viewModel: VehicleDetailsViewModel = koinViewModel()) {

  val vehicle by viewModel.vehicle.collectAsState()
  val context = LocalContext.current

  val activity = context.findActivity()
  val intent = activity?.intent

  val scope = rememberCoroutineScope()

  LaunchedEffect(key1 = intent?.getStringExtra(INTENT_VEHICLE_ID)) {
    val vehicleId = intent?.getStringExtra(INTENT_VEHICLE_ID)
    Timber.tag("MainActivity").i("User Id: $vehicleId")
    if (vehicleId != null) {
      withContext(Dispatchers.IO) { viewModel.getVehicle(vehicleId = vehicleId) }
    }
  }

  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      val imageUri = result.uriContent ?: return@rememberLauncherForActivityResult
      val vehicleId = vehicle?.id ?: return@rememberLauncherForActivityResult
      scope.launch {
        viewModel.uploadVehiclePhoto(vehicleId, imageUri)
        Timber.tag("VehicleDetailsScreen").i("VehicleDetailsScreen: $imageUri")
      }
    }
  )

  val launcherRequestCameraPermisison = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { isGranted ->
      if (isGranted) {
        val options = CropImageContractOptions(
          null,
          CropImageOptions(
            imageSourceIncludeGallery = true,
            imageSourceIncludeCamera = true,
            guidelines = CropImageView.Guidelines.ON,
            aspectRatioX = 2,
            aspectRatioY = 1
          )
        )
        takePicture.launch(options)
      }
    }
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(text = stringResource(R.string.vehicle_details_text))
        },
        actions = {
          IconButton(onClick = {
            val openAddMaintenance = Intent(context, AddMaintenanceActivity::class.java)
            openAddMaintenance.putExtra(INTENT_VEHICLE_ID, vehicle?.id)
            context.startActivity(openAddMaintenance)
          }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
          }
        })
    },
  ) { contentPadding ->
    Column(
      Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(bottom = 16.dp)
    ) {
      CardVehicleDetails(
        vehicle = vehicle ?: vehicleSample,
        onClick = {
          launcherRequestCameraPermisison.launch(Manifest.permission.CAMERA)
        }
      )
      Divider(modifier = Modifier.padding(vertical = 16.dp))
      Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = stringResource(R.string.maintenance_text),
        style = Typography.h5
      )

      val maintenances by viewModel.maintenances.collectAsState()

      if (maintenances.isNotEmpty()) {
        MaintenanceListComponent(maintenances, activity, context)
      } else {
        Column(
          modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.does_not_have_maintenance),
            style = Typography.h5,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}

@Composable
private fun MaintenanceListComponent(
  maintenances: List<Maintenance>,
  activity: Activity?,
  context: Context,
  viewModel: VehicleDetailsViewModel = koinViewModel()
) {
  
  val scope = rememberCoroutineScope()
  LazyColumn {
    items(maintenances) { maintenance ->
      SimpleCardMaintenance(
        modifier = Modifier.fillMaxWidth(),
        maintenance = maintenance,
        onClick = {
          val openEditMaintenance = Intent(activity, AddMaintenanceActivity::class.java)
          openEditMaintenance.putExtra(
            context.getString(R.string.edit_maintenance_key),
            maintenance
          )
          activity?.startActivity(openEditMaintenance)
        },
        onLongClick = {
          scope.launch { viewModel.deleteMaintenance(it) }
        }
      )
      Spacer(modifier = Modifier.padding(bottom = 8.dp))

    }
  }


}

@Composable

@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_YES
)
fun VehicleDetailsScreenPreview() {
//  VehicleDetailsScreen(vehicleSample, viewModel)
}
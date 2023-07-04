package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.Manifest
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen.AddMaintenanceActivity
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import br.com.gabrielmorais.autocare.ui.components.CardVehicleDetails
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_USER_ID
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_VEHICLE_ID
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import org.koin.androidx.viewmodel.ext.android.viewModel

class VehicleDetailsActivity : ComponentActivity() {

  private val viewModel: VehicleDetailsViewModel by viewModel()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AutoCareTheme {
        VehicleDetailsScreen(viewModel)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    val extras = intent.extras
    extras?.let { bundle ->
      val userId = bundle.getString(INTENT_USER_ID)
      val vehicleId = bundle.getString(INTENT_VEHICLE_ID)
      if (userId != null && vehicleId != null) {
        viewModel.setUserid(userId)
        viewModel.getVehicle(userId, vehicleId)
      }
    }
  }
}

@Composable
fun VehicleDetailsScreen(viewModel: VehicleDetailsViewModel) {

  val vehicle = viewModel.vehicle.collectAsState()
  val userId = viewModel.userId.collectAsState()
  val context = LocalContext.current
  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      val imageUri = result.uriContent
      imageUri?.let { image ->
        viewModel.uploadVehiclePhoto(
          userId.value,
          vehicle.value?.id!!,
          image
        )
        Log.i("VehicleDetailsScreen", "VehicleDetailsScreen: $image")
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
        })
    },
    floatingActionButton = {
      FloatingActionButton(onClick = {
        val intent = Intent(context, AddMaintenanceActivity::class.java)
        context.startActivity(intent)
      }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { contentPadding ->
    Column(
      Modifier
        .fillMaxSize()
        .padding(contentPadding)
        .padding(16.dp)
    ) {
      CardVehicleDetails(
        vehicle = vehicle.value ?: vehicleSample,
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
      vehicle.value?.maintenanceRecord?.let { maintenanceList ->
        LazyColumn {
          items(maintenanceList) { maintenance ->
            SimpleCardMaintenance(
              modifier = Modifier
                .fillMaxWidth(),
              maintenance = maintenance
            )
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
          }
        }
      } ?: Column(
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

@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_YES
)
@Composable
fun VehicleDetailsScreenPreview() {
//  VehicleDetailsScreen(vehicleSample, viewModel)
}
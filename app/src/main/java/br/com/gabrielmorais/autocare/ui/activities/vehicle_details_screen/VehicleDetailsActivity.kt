package br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen

import android.Manifest
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.notifications.NotificationUtils
import br.com.gabrielmorais.autocare.sampleData.vehicleSample
import br.com.gabrielmorais.autocare.ui.activities.add_maintenance_screen.AddMaintenanceActivity
import br.com.gabrielmorais.autocare.ui.activities.maintenance_screen.SimpleCardMaintenance
import br.com.gabrielmorais.autocare.ui.components.CardVehicleDetails
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_VEHICLE_ID
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        message?.let {
          Toast.makeText(this@VehicleDetailsActivity, it, Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    val extras = intent.extras
    extras?.let { bundle ->
      val vehicleId = bundle.getString(INTENT_VEHICLE_ID)
      if (vehicleId != null) {
        viewModel.getVehicle(vehicleId)
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VehicleDetailsScreen(viewModel: VehicleDetailsViewModel) {

  val vehicle = viewModel.vehicle.collectAsState()
  val userId = viewModel.userId.collectAsState()
  val context = LocalContext.current
  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      val imageUri = result.uriContent
      val vehicleId = vehicle.value?.id
      if (imageUri != null && vehicleId != null) {
        viewModel.uploadVehiclePhoto(userId.value, vehicleId, imageUri)
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
            val intent = Intent(context, AddMaintenanceActivity::class.java)
            intent.putExtra(INTENT_VEHICLE_ID, vehicle.value?.id)
            context.startActivity(intent)
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
      vehicle.value?.maintenances?.takeIf { it.isNotEmpty() }?.let { maintenanceList ->
        LazyColumn {
          items(maintenanceList, key = { it.id }) { maintenance ->
            val dismissState = rememberDismissState(
              confirmStateChange = { value ->
                if (value == DismissValue.DismissedToStart) {
                  viewModel.deleteMaintenance(maintenance) { removed ->
                    NotificationUtils.cancelNotification(context, removed)
                  }
                }
                true
              }
            )
            SwipeToDismiss(
              state = dismissState,
              directions = setOf(DismissDirection.EndToStart),
              dismissThresholds = { FractionalThreshold(0.66F) },
              background = {
                val color = when (dismissState.dismissDirection) {
                  DismissDirection.EndToStart -> Color.Red
                  else -> Color.Transparent
                }
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                ) {
                  Icon(
                    modifier = Modifier
                      .align(Alignment.CenterEnd)
                      .padding(end = 10.dp)
                      .size(25.dp),
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                  )
                }
              }
            ) {
              SimpleCardMaintenance(
                modifier = Modifier.fillMaxWidth(),
                maintenance = maintenance
              )
            }
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
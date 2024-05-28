package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import br.com.gabrielmorais.autocare.sampleData.userSample
import br.com.gabrielmorais.autocare.ui.activities.login_screen.LoginActivity
import br.com.gabrielmorais.autocare.ui.activities.my_account_screen.MyAccountActivity
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_USER_ID
import br.com.gabrielmorais.autocare.utils.Constants.INTENT_VEHICLE_ID
import br.com.gabrielmorais.autocare.utils.findActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
//      val notificationPermission = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { isGranted ->
//          if (isGranted) {
//            Toast.makeText(this@MainActivity, "Permissão aceita", Toast.LENGTH_SHORT).show()
//          }
//        }
//      )
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
//      }
//      NotificationUtils.createNotificationChannel(applicationContext)
      AutoCareTheme {
        MainScreen()
      }

    }
  }
}


@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
  val scaffoldState = rememberScaffoldState()
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val activity = context.findActivity()
  val intent = activity?.intent
  val user by viewModel.user.collectAsState(initial = null)
  val vehicleList by viewModel.vehicleList.collectAsState()

  LaunchedEffect(key1 = intent?.getStringExtra(INTENT_USER_ID)) {
    val userId = intent?.getStringExtra(INTENT_USER_ID)
    Timber.tag("MainActivity").i("User Id: $userId")
    if (userId != null) {
      withContext(Dispatchers.IO) { viewModel.getUser(userId = userId) }
    }
  }

  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      val imageUri = result.uriContent
      val userId = user?.id!!
      imageUri?.let { image ->
        scope.launch { viewModel.updateUserPhoto(userId, image) }
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
            aspectRatioX = 1,
            aspectRatioY = 1
          )
        )
        takePicture.launch(options)
      }
    }
  )

  Timber.tag("Lista de veículos'").i("MainScreen: $vehicleList")

  Scaffold(
    scaffoldState = scaffoldState,
    topBar = {
      TopBar(scaffoldState = scaffoldState, viewModel = viewModel)
    },
    drawerGesturesEnabled = true,
    drawerContent = {
      DrawerContent(user) {
        launcherRequestCameraPermisison.launch(Manifest.permission.CAMERA)
      }
    }
  ) { contentPadding ->
    Column(Modifier.padding(contentPadding)) {

    }
    if (vehicleList.isNotEmpty()) {
      LazyColumn(
        modifier = Modifier
          .padding(contentPadding)
          .scrollable(state = scrollState, orientation = Orientation.Vertical)
      ) {
        items(vehicleList) { vehicle ->
          CardVehicle(
            vehicle = vehicle,
            modifier = Modifier
              .fillMaxWidth()
              .padding(5.dp)
              .clip(shape = RoundedCornerShape(15.dp)),
            onCardClick = {
              val userId = user?.id
              val vehicleId = vehicle.id
              val openVehicleDetails = Intent(context, VehicleDetailsActivity::class.java)
              openVehicleDetails.putExtra(INTENT_USER_ID, userId)
              openVehicleDetails.putExtra(INTENT_VEHICLE_ID, vehicleId)
              context.startActivity(openVehicleDetails)
            }
          )
        }
      }
    } else {
      EmptyCarList(contentPadding)
    }

  }
}

@Composable
fun EmptyCarList(contentPadding: PaddingValues) {
  Column(
    modifier = Modifier
      .padding(contentPadding)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .weight(1F)
        .wrapContentHeight(Alignment.CenterVertically)
    ) {
      Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.text_any_car_registered),
        textAlign = TextAlign.Center,
        style = Typography.h4
      )
    }
  }
}

@Composable
fun TopBar(scaffoldState: ScaffoldState, viewModel: MainViewModel?) {
  val scope = rememberCoroutineScope()
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.app_name)) },
    navigationIcon = {
      IconButton(onClick = {
        scope.launch {
          scaffoldState.drawerState.apply {
            if (isClosed) {
              open()
            } else {
              close()
            }
          }
        }
      }) {
        Icon(imageVector = Icons.Default.Menu, null)
      }
    },
    actions = { TopAppBarActions() })

}

@Composable
fun DrawerContent(user: User? = null, updateUserPhoto: () -> Unit = {}) {
  val context = LocalContext.current

  Timber.tag("MainScreen").i("MainScreen: DrawerContent ${user?.name}")
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AsyncImage(
      modifier = Modifier
        .fillMaxHeight(0.3F)
        .padding(top = 5.dp, bottom = 15.dp)
        .clickable(onClick = updateUserPhoto),
      contentScale = ContentScale.Fit,
      alignment = Alignment.Center,
      model = ImageRequest
        .Builder(LocalContext.current)
        .data(user?.photo ?: userSample.photo)
        .crossfade(true)
        .transformations(CircleCropTransformation())
        .build(),
      contentDescription = stringResource(R.string.profile_image_description)
    )
    Text(
      text = user?.name ?: stringResource(R.string.text_unknow),
      style = TextStyle(fontSize = 25.sp)
    )
  }

  TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
    val intent = Intent(context, MyAccountActivity::class.java)
    intent.putExtra(INTENT_USER_ID, user?.id)
    context.startActivity(intent)
  }) {
    Text(text = stringResource(id = R.string.text_my_account), style = Typography.h5)
  }
//  TextButton(modifier = Modifier.fillMaxWidth(), onClick = { }) {
//    Text(text = stringResource(id = R.string.text_maintenance), style = Typography.h6)
//  }
}

@Composable
fun TopAppBarActions(viewModel: MainViewModel = koinViewModel()) {
  var showDropDownMenu by remember { mutableStateOf(false) }
  val context = LocalContext.current as ComponentActivity
  IconButton(onClick = { showDropDownMenu = !showDropDownMenu }) {
    Icon(imageVector = Icons.Rounded.MoreVert, null)
  }
  DropdownMenu(
    expanded = showDropDownMenu,
    onDismissRequest = { showDropDownMenu = false }) {
    TextButton(onClick = {
      viewModel.logout()
      val intent = Intent(context, LoginActivity::class.java)
      context.startActivity(intent)
      context.finish()
    }) {
      Text(text = stringResource(R.string.text_exit))
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  MainScreen()
}


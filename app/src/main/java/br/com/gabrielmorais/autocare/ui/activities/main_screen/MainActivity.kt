package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.images.CloudinaryTransformations
import br.com.gabrielmorais.autocare.data.images.CloudinaryUrl
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.notifications.BootReceiver
import br.com.gabrielmorais.autocare.ui.activities.login_screen.LoginActivity
import br.com.gabrielmorais.autocare.ui.activities.my_account_screen.MyAccountActivity
import br.com.gabrielmorais.autocare.ui.activities.vehicle_details_screen.VehicleDetailsActivity
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.theme.AutoCareTheme
import br.com.gabrielmorais.autocare.ui.theme.Typography
import br.com.gabrielmorais.autocare.utils.Constants.Companion.INTENT_VEHICLE_ID
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
  private val viewModel by viewModel<MainViewModel>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val notificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
      )
      // Pedir a permissao no corpo da composicao e efeito colateral: era
      // disparado de novo a cada recomposicao. LaunchedEffect(Unit) roda uma vez.
      LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.POST_NOTIFICATIONS
          ) != PackageManager.PERMISSION_GRANTED
        ) {
          notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
      }
      AutoCareTheme {
        MainScreen(viewModel)
      }

    }

    // Cobre o caso de o BOOT_COMPLETED nao ter chegado (app forcado a parar,
    // instalacao nova) mantendo os alarmes consistentes ao abrir o app.
    BootReceiver.enqueueReschedule(this)

    lifecycleScope.launch {
      viewModel.message.collectLatest { message ->
        if (message.isNotBlank()) {
          Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
      }
    }

    // Uma vez so: o Flow do repositorio mantem a tela atualizada sozinho.
    // Antes isso rodava em onResume e cada retorno a tela deixava para tras
    // mais um ValueEventListener ativo.
    viewModel.observeUser()
  }
}


@Composable
fun MainScreen(viewModel: MainViewModel? = null) {
  val scaffoldState = rememberScaffoldState()
  val scrollState = rememberScrollState()
  val context = LocalContext.current
  val user = viewModel?.user?.collectAsState(initial = null)
  val vehicleList = user?.value?.vehicles

  val takePicture = rememberLauncherForActivityResult(
    contract = CropImageContract(),
    onResult = { result ->
      result.uriContent?.let { imageUri -> viewModel?.updateUserPhoto(imageUri) }
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


  Scaffold(
    scaffoldState = scaffoldState,
    topBar = {
      TopBar(scaffoldState = scaffoldState, viewModel = viewModel)
    },
    drawerGesturesEnabled = true,
    drawerContent = {
      DrawerContent(user?.value) {
        launcherRequestCameraPermisison.launch(Manifest.permission.CAMERA)
      }
    }
  ) { contentPadding ->
    if (!vehicleList.isNullOrEmpty()) {
      LazyColumn(
        modifier = Modifier
          .padding(contentPadding)
          .scrollable(scrollState, orientation = Orientation.Vertical)
      ) {
        items(vehicleList) { vehicle ->
          CardVehicle(
            vehicle = vehicle,
            modifier = Modifier
              .fillMaxWidth()
              .padding(5.dp)
              .clip(shape = RoundedCornerShape(15.dp)),
            onCardClick = {
              val intent = Intent(context, VehicleDetailsActivity::class.java)
              intent.putExtra(INTENT_VEHICLE_ID, vehicle.id)
              context.startActivity(intent)
            }
          )
        }
      }
    } else
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
    actions = { TopAppBarActions(viewModel) })
}

private val AVATAR_SIZE = 120.dp

@Composable
fun DrawerContent(user: User? = null, updateUserPhoto: () -> Unit = {}) {
  val context = LocalContext.current

  // Painter unico para os tres estados: sem foto, carregando e falha. Sem ele o
  // AsyncImage sem tamanho colapsava para 0x0 e o drawer ficava visualmente vazio.
  val avatarFallback = rememberVectorPainter(Icons.Outlined.Person)

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AsyncImage(
      modifier = Modifier
        .padding(vertical = 16.dp)
        // O tamanho explicito e o que garante que a area exista mesmo quando a
        // imagem nao carrega.
        .size(AVATAR_SIZE)
        .clip(CircleShape)
        .clickable(onClick = updateUserPhoto),
      contentScale = ContentScale.Crop,
      alignment = Alignment.Center,
      model = ImageRequest
        .Builder(LocalContext.current)
        .data(CloudinaryUrl.withTransformation(user?.photo, CloudinaryTransformations.AVATAR))
        .crossfade(true)
        // Recorte pelo clip e nao por transformacao: transformacao so se aplica
        // ao bitmap carregado, entao o fallback ficaria quadrado.
        .build(),
      placeholder = avatarFallback,
      error = avatarFallback,
      fallback = avatarFallback,
      contentDescription = stringResource(R.string.profile_image_description)
    )
    Text(
      // O cadastro grava apenas id e e-mail, entao name e nulo para todo mundo
      // que nunca editou o perfil: mostrar o e-mail informa mais que "Desconhecido".
      text = user?.name?.takeIf { it.isNotBlank() }
        ?: user?.email
        ?: stringResource(R.string.text_unknow),
      style = TextStyle(fontSize = 20.sp)
    )
  }

  TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
    context.startActivity(Intent(context, MyAccountActivity::class.java))
  }) {
    Text(text = stringResource(id = R.string.text_my_account), style = Typography.h6)
  }
}

@Composable
fun TopAppBarActions(viewModel: MainViewModel?) {
  var showDropDownMenu by remember { mutableStateOf(false) }
  val context = LocalContext.current as ComponentActivity
  IconButton(onClick = { showDropDownMenu = !showDropDownMenu }) {
    Icon(imageVector = Icons.Rounded.MoreVert, null)
  }
  DropdownMenu(
    expanded = showDropDownMenu,
    onDismissRequest = { showDropDownMenu = false }) {
    TextButton(onClick = {
      viewModel?.logout()
      // LoginActivity ja tinha se finalizado ao abrir a Main, entao o finish()
      // sozinho esvaziava a pilha e fechava o app em vez de voltar ao login.
      val intent = Intent(context, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }
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


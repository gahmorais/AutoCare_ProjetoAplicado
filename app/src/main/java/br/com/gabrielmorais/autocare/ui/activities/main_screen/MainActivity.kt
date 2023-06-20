package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.ui.components.CardVehicle
import br.com.gabrielmorais.autocare.ui.components.vehicleSample
import br.com.gabrielmorais.autocare.ui.viewmodels.MainViewModel
import br.com.gabrielmorais.autocare.ui.viewmodels.factory.MainViewModelFactory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private val viewModel by viewModels<MainViewModel> {
    MainViewModelFactory(AuthRepositoryImpl(Firebase.auth))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainScreen(viewModel)
    }
  }
}

@Composable
fun MainScreen(viewModel: MainViewModel? = null) {
  val scaffoldState = rememberScaffoldState()
  val scrollState = rememberScrollState()
  Scaffold(
    scaffoldState = scaffoldState,
    topBar = {
      TopBar(scaffoldState = scaffoldState, viewModel = viewModel)
    },
    drawerGesturesEnabled = true,
    drawerContent = { DrawerContent() }
  ) { contentPadding ->
    Column(
      Modifier
        .padding(contentPadding)
        .verticalScroll(scrollState),
    ) {
      repeat(10) {
        CardVehicle(
          vehicle = vehicleSample,
          modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(shape = RoundedCornerShape(15.dp)),
          onClick = {

          }
        )
      }
    }
  }
}

@Composable
fun TopBar(scaffoldState: ScaffoldState, viewModel: MainViewModel?) {
  val scope = rememberCoroutineScope()
  TopAppBar(
    title = { Text(text = "AutoCare") },
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

@Composable
fun DrawerContent() {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    AsyncImage(
      modifier = Modifier
        .padding(vertical = 5.dp),
      contentScale = ContentScale.Fit,
      alignment = Alignment.Center,
      model = ImageRequest
        .Builder(LocalContext.current)
        .data("https://static.catapult.co/cdn-cgi/image/width=1170,height=658,dpr=2,fit=cover,format=auto/production/stories/31705/cover_photos/original/iron_man_site_1633028435_1637683340.jpg")
        .crossfade(true)
        .transformations(CircleCropTransformation())
        .build(),
      contentDescription = "Profile Image"
    )
    Text(text = "Usuário 1", style = TextStyle(fontSize = 25.sp))
  }

  TextButton(modifier = Modifier.fillMaxWidth(), onClick = { }) {
    Text(text = "Minha conta")
  }
  TextButton(modifier = Modifier.fillMaxWidth(), onClick = { }) {
    Text(text = "Manutenções")
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
      context.finish()
    }) {
      Text(text = "Sair")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
  MainScreen()
}


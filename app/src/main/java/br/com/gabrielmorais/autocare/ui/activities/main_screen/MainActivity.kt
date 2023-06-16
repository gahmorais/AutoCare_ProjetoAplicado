package br.com.gabrielmorais.autocare.ui.activities.main_screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import br.com.gabrielmorais.autocare.data.repository.authorization.AuthRepositoryImpl
import br.com.gabrielmorais.autocare.ui.viewmodels.MainViewModel
import br.com.gabrielmorais.autocare.ui.viewmodels.factory.MainViewModelFactory
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
  val scope = rememberCoroutineScope()
  Scaffold(
    scaffoldState = scaffoldState,
    topBar = {
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
    },
    drawerGesturesEnabled = true,
    drawerContent = { DrawerContent() }
  ) { contentPadding ->
    Column(Modifier.padding(contentPadding)) {

    }
  }
}

@Composable
fun DrawerContent() {
  TextButton(modifier = Modifier.fillMaxWidth(), onClick = {  }) {
    Text(text = "Minha conta")
  }
  TextButton(modifier = Modifier.fillMaxWidth(), onClick = {  }) {
    Text(text = "Histórico de manutenção")
  }
  TextButton(modifier = Modifier.fillMaxWidth(), onClick = {  }) {
    Text(text = "Cadastrar nova manutenção")
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
      context.finishActivity(0)
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


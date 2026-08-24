package br.com.gabrielmorais.autocare.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.R

@Composable
fun DefaultSnackBar(
  modifier: Modifier = Modifier,
  snackbarHostState: SnackbarHostState? = null,
  onDismiss: () -> Unit = {}
) {
  // O !! aqui quebrava o @Preview, que chama o componente sem hostState.
  val hostState = snackbarHostState ?: return
  SnackbarHost(
    modifier = modifier
      .fillMaxWidth()
      .wrapContentHeight(Alignment.Bottom),
    hostState = hostState,
    snackbar = { data ->
      // No M3 mensagem e rotulo de acao passaram a viver em data.visuals.
      Snackbar(modifier = Modifier.padding(8.dp),
        content = {
          Text(data.visuals.message)
        },
        action = {
          data.visuals.actionLabel?.let {
            TextButton(onClick = onDismiss) {
              Text(text = stringResource(R.string.text_close))
            }
          }
        })
    })
}


@Preview(showBackground = true)
@Composable
fun DefaultSnackBarPreview() {
  Box {
    DefaultSnackBar()
  }
}
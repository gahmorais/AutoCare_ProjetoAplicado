package br.com.geasoftware.autocare.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.geasoftware.autocare.data.ads.LocalAdsController

/**
 * Banner ancorado no rodape. So entra em Veiculos e no Detalhe do veiculo:
 * nada de anuncio no formulario, no primeiro acesso nem no backup, onde um toque
 * acidental e facil (politica da Play).
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
  LocalAdsController.current.Banner(modifier)
}

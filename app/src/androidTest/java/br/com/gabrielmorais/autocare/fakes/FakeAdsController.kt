package br.com.gabrielmorais.autocare.fakes

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.gabrielmorais.autocare.data.ads.AdsController
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Anuncios sem SDK e sem rede: o banner e um Box marcado com [BANNER_TAG], e
 * os intersticiais pedidos so sao contados.
 */
class FakeAdsController : AdsController {
  override val canShowAds = MutableStateFlow(true)
  override val privacyOptionsRequired = MutableStateFlow(false)

  var interstitialRequests = 0
    private set

  override fun onActivityCreated(activity: Activity, newSession: Boolean) = Unit

  @Composable
  override fun Banner(modifier: Modifier) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(50.dp)
        .testTag(BANNER_TAG)
    )
  }

  override fun maybeShowInterstitial(activity: Activity) {
    interstitialRequests++
  }

  override fun showPrivacyOptions(activity: Activity) = Unit

  companion object {
    const val BANNER_TAG = "ad-banner"
  }
}

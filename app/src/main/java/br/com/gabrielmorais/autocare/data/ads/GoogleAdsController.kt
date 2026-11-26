package br.com.gabrielmorais.autocare.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

data class AdUnitIds(val banner: String, val interstitial: String)

/** So no debug: forca o formulario de consentimento como se o aparelho estivesse no EEE. */
data class ConsentDebug(val forceEea: Boolean, val testDeviceHashedId: String)

/**
 * UMP + AdMob. A ordem importa e e a exigida pelo UMP: atualizar o
 * consentimento, mostrar o formulario se for preciso e so inicializar o SDK
 * depois de [ConsentInformation.canRequestAds]. Antes disso nenhum anuncio e pedido.
 */
class GoogleAdsController(
  private val appContext: Context,
  private val ids: AdUnitIds,
  private val policy: InterstitialPolicy,
  private val historyStore: InterstitialHistoryStore,
  private val consentDebug: ConsentDebug
) : AdsController {

  private val consentInformation: ConsentInformation =
    UserMessagingPlatform.getConsentInformation(appContext)
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val initialized = AtomicBoolean(false)

  private val _canShowAds = MutableStateFlow(false)
  override val canShowAds: StateFlow<Boolean> = _canShowAds

  private val _privacyOptionsRequired = MutableStateFlow(false)
  override val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired

  private var interstitial: InterstitialAd? = null
  private var loadingInterstitial = false

  override fun onActivityCreated(activity: Activity, newSession: Boolean) {
    if (!newSession) return
    historyStore.save(policy.startSession(historyStore.load()))

    consentInformation.requestConsentInfoUpdate(
      activity,
      consentParameters(),
      {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
          formError?.let { Log.w(TAG, "Formulario de consentimento: ${it.message}") }
          refreshPrivacyOptions()
          initializeIfAllowed()
        }
      },
      { requestError ->
        // Sem rede, o UMP usa o consentimento da ultima vez; se ele ja permitia
        // anuncios, segue.
        Log.w(TAG, "Atualizacao do consentimento: ${requestError.message}")
        initializeIfAllowed()
      }
    )
    // Quem ja consentiu numa abertura anterior nao espera a ida a rede.
    initializeIfAllowed()
  }

  private fun consentParameters(): ConsentRequestParameters {
    val builder = ConsentRequestParameters.Builder()
    if (consentDebug.forceEea) {
      val debug = ConsentDebugSettings.Builder(appContext)
        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
      consentDebug.testDeviceHashedId.takeIf { it.isNotBlank() }?.let(debug::addTestDeviceHashedId)
      builder.setConsentDebugSettings(debug.build())
    }
    return builder.build()
  }

  private fun refreshPrivacyOptions() {
    _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
      ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
  }

  private fun initializeIfAllowed() {
    if (!consentInformation.canRequestAds()) return
    if (!initialized.compareAndSet(false, true)) return
    scope.launch {
      // A inicializacao faz I/O e leva centenas de ms: fora da main thread.
      withContext(Dispatchers.IO) { MobileAds.initialize(appContext) {} }
      _canShowAds.value = true
      loadInterstitial()
    }
  }

  private fun loadInterstitial() {
    if (interstitial != null || loadingInterstitial || !_canShowAds.value) return
    loadingInterstitial = true
    InterstitialAd.load(
      appContext,
      ids.interstitial,
      AdRequest.Builder().build(),
      object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(ad: InterstitialAd) {
          loadingInterstitial = false
          interstitial = ad
        }

        override fun onAdFailedToLoad(error: LoadAdError) {
          loadingInterstitial = false
          Log.w(TAG, "Intersticial nao carregou: ${error.message}")
        }
      }
    )
  }

  override fun maybeShowInterstitial(activity: Activity) {
    // Pre-carregado: se nao estiver pronto, simplesmente nao aparece. Nunca se
    // espera o anuncio carregar, para nao travar a volta ao detalhe do veiculo.
    val ad = interstitial ?: return loadInterstitial()
    val history = historyStore.load()
    if (!policy.canShow(history)) return

    interstitial = null
    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdShowedFullScreenContent() {
        historyStore.save(policy.recordShown(historyStore.load()))
      }

      override fun onAdDismissedFullScreenContent() = loadInterstitial()

      override fun onAdFailedToShowFullScreenContent(error: AdError) = loadInterstitial()
    }
    ad.show(activity)
  }

  override fun showPrivacyOptions(activity: Activity) {
    UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
      formError?.let { Log.w(TAG, "Opcoes de privacidade: ${it.message}") }
      refreshPrivacyOptions()
      // Quem recusou e voltou atras passa a poder ver anuncios; quem retirou o
      // consentimento deixa de receber pedidos (canRequestAds fica false).
      initializeIfAllowed()
    }
  }

  @Composable
  override fun Banner(modifier: Modifier) {
    val ready by canShowAds.collectAsState()
    if (ready) {
      BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt()
        val context = LocalContext.current
        val adSize = remember(widthDp) {
          AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        // Altura reservada desde o inicio: o conteudo nao pula quando o anuncio chega.
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(adSize.height.dp),
          contentAlignment = Alignment.Center
        ) {
          // key: largura nova (rotacao) pede um AdView novo, com o tamanho certo.
          key(adSize) {
            AndroidView(
              factory = { viewContext ->
                AdView(viewContext).apply {
                  setAdSize(adSize)
                  adUnitId = ids.banner
                  loadAd(AdRequest.Builder().build())
                }
              },
              onRelease = { it.destroy() }
            )
          }
        }
      }
    }
  }

  private companion object {
    const val TAG = "Ads"
  }
}

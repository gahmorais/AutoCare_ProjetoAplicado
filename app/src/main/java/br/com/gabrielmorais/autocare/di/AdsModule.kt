package br.com.gabrielmorais.autocare.di

import br.com.gabrielmorais.autocare.BuildConfig
import br.com.gabrielmorais.autocare.data.ads.AdUnitIds
import br.com.gabrielmorais.autocare.data.ads.AdsController
import br.com.gabrielmorais.autocare.data.ads.ConsentDebug
import br.com.gabrielmorais.autocare.data.ads.GoogleAdsController
import br.com.gabrielmorais.autocare.data.ads.InterstitialHistoryStore
import br.com.gabrielmorais.autocare.data.ads.InterstitialPolicy
import br.com.gabrielmorais.autocare.data.ads.NoAdsController
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Os IDs vem do build type: de teste no debug, reais so na release assinada. */
val adsModule = module {
  single<AdsController> {
    // So o debug com -PsemAnuncios=true cai aqui (capturas de tela da loja).
    if (!BuildConfig.ADS_ENABLED) return@single NoAdsController
    GoogleAdsController(
      appContext = androidContext(),
      ids = AdUnitIds(
        banner = BuildConfig.ADMOB_BANNER_ID,
        interstitial = BuildConfig.ADMOB_INTERSTITIAL_ID
      ),
      policy = InterstitialPolicy(),
      historyStore = InterstitialHistoryStore(androidContext()),
      consentDebug = ConsentDebug(
        forceEea = BuildConfig.UMP_DEBUG_EEA,
        testDeviceHashedId = BuildConfig.UMP_TEST_DEVICE_ID
      )
    )
  }
}

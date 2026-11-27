package br.com.geasoftware.autocare.di

import br.com.geasoftware.autocare.BuildConfig
import br.com.geasoftware.autocare.data.ads.AdUnitIds
import br.com.geasoftware.autocare.data.ads.AdsController
import br.com.geasoftware.autocare.data.ads.ConsentDebug
import br.com.geasoftware.autocare.data.ads.GoogleAdsController
import br.com.geasoftware.autocare.data.ads.InterstitialHistoryStore
import br.com.geasoftware.autocare.data.ads.InterstitialPolicy
import br.com.geasoftware.autocare.data.ads.NoAdsController
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

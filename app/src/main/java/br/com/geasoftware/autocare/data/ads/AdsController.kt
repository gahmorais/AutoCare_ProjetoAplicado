package br.com.geasoftware.autocare.data.ads

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tudo o que o app faz com anuncios passa por aqui. A implementacao real
 * ([GoogleAdsController]) fala com o UMP e o AdMob; os testes usam uma fake,
 * para nenhum teste depender de rede nem do SDK.
 */
interface AdsController {
  /** O consentimento permite pedir anuncios e o SDK ja foi inicializado. */
  val canShowAds: StateFlow<Boolean>

  /** O UMP exige um ponto de entrada para rever o consentimento (Perfil). */
  val privacyOptionsRequired: StateFlow<Boolean>

  /**
   * Na abertura do app: atualiza o consentimento, mostra o formulario se for
   * preciso e so entao inicializa o SDK. [newSession] e false quando a Activity
   * so foi recriada (rotacao), para nao contar sessao nem pedir consentimento de novo.
   */
  fun onActivityCreated(activity: Activity, newSession: Boolean)

  /** Banner adaptativo ancorado. Nao ocupa espaco enquanto nao ha anuncio a pedir. */
  @Composable
  fun Banner(modifier: Modifier)

  /** Depois de salvar uma manutencao. A [InterstitialPolicy] decide se aparece. */
  fun maybeShowInterstitial(activity: Activity)

  fun showPrivacyOptions(activity: Activity)
}

/** Sem anuncios: padrao do [LocalAdsController], usado pelas telas em teste e em preview. */
object NoAdsController : AdsController {
  override val canShowAds: StateFlow<Boolean> = MutableStateFlow(false)
  override val privacyOptionsRequired: StateFlow<Boolean> = MutableStateFlow(false)
  override fun onActivityCreated(activity: Activity, newSession: Boolean) = Unit

  @Composable
  override fun Banner(modifier: Modifier) = Unit
  override fun maybeShowInterstitial(activity: Activity) = Unit
  override fun showPrivacyOptions(activity: Activity) = Unit
}

/** A MainActivity fornece o controlador real; fora dela, nenhum anuncio. */
val LocalAdsController = staticCompositionLocalOf<AdsController> { NoAdsController }

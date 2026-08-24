package br.com.gabrielmorais.autocare.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset

/**
 * Duracoes e easings do M3 num lugar so. O Compose Material 3 1.1.1 nao expoe
 * MotionScheme, entao os tokens vivem aqui em vez de virarem numeros soltos
 * espalhados pelas telas.
 */
object Motion {
  /** short4: micro-interacoes e mudancas de estado. */
  const val SHORT = 200

  /** medium2: entra e sai de tela, expansao, revelacao. */
  const val MEDIUM = 300

  /** Emphasized decelerate - o que o M3 usa para conteudo entrando. */
  val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

  /** Emphasized accelerate - conteudo saindo. */
  val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
}

/**
 * O Compose nao tem um LocalReducedMotion pronto. A fonte de verdade no Android
 * e ANIMATOR_DURATION_SCALE, que vai a zero quando o usuario desliga animacoes
 * nas opcoes de acessibilidade ou de desenvolvedor.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
@ReadOnlyComposable
private fun readAnimatorScale(): Float {
  val resolver = LocalContext.current.contentResolver
  return Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
}

/**
 * Lido uma vez por Activity: `staticCompositionLocalOf` mais `remember` sem
 * chave. Trocar a configuracao com o app aberto e raro o bastante para nao
 * justificar um ContentObserver.
 */
@Composable
fun rememberReducedMotion(): Boolean {
  val scale = readAnimatorScale()
  return remember(scale) { scale == 0f }
}

/**
 * Spec do reposicionamento de itens de lista - o que faz uma manutencao marcada
 * como concluida deslizar ate o fim em vez de saltar. Com animacoes desligadas
 * vira `snap`, que reposiciona sem percurso.
 */
@Composable
fun itemPlacementSpec(): FiniteAnimationSpec<IntOffset> =
  if (LocalReducedMotion.current) snap() else spring(stiffness = Spring.StiffnessMediumLow)

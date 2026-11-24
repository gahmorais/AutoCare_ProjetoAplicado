package br.com.gabrielmorais.autocare.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import br.com.gabrielmorais.autocare.ui.theme.LocalReducedMotion
import br.com.gabrielmorais.autocare.ui.theme.Motion

private typealias NavScope = AnimatedContentTransitionScope<NavBackStackEntry>

/**
 * As quatro transicoes do NavHost, resolvidas a partir de quem sai e quem entra.
 *
 * - Entre abas: fade-through. As abas sao irmas, nao ha "para frente".
 * - Abrir detalhe/formulario e voltar: shared axis X. A tela nova entra pela
 *   direita e o voltar inverte o sentido, o que diz ao usuario onde ele esta na
 *   pilha.
 *
 * O Compose nao honra ANIMATOR_DURATION_SCALE sozinho, entao com animacoes
 * desligadas tudo vira None - sem isso o app animaria para quem pediu que nao.
 */
class NavTransitions(
  private val reducedMotion: Boolean,
  private val axisOffsetPx: Int
) {
  val enter: NavScope.() -> EnterTransition = {
    when {
      reducedMotion -> EnterTransition.None
      isBetweenTabs() -> fadeThroughIn()
      else -> axisIn(forward = true)
    }
  }

  val exit: NavScope.() -> ExitTransition = {
    when {
      reducedMotion -> ExitTransition.None
      isBetweenTabs() -> fadeThroughOut()
      else -> axisOut(forward = true)
    }
  }

  val popEnter: NavScope.() -> EnterTransition = {
    when {
      reducedMotion -> EnterTransition.None
      isBetweenTabs() -> fadeThroughIn()
      else -> axisIn(forward = false)
    }
  }

  val popExit: NavScope.() -> ExitTransition = {
    when {
      reducedMotion -> ExitTransition.None
      isBetweenTabs() -> fadeThroughOut()
      else -> axisOut(forward = false)
    }
  }

  private fun axisIn(forward: Boolean): EnterTransition =
    slideInHorizontally(
      animationSpec = tween(Motion.MEDIUM, easing = Motion.EmphasizedDecelerate),
      initialOffsetX = { if (forward) axisOffsetPx else -axisOffsetPx }
    ) + fadeIn(tween(FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = LinearEasing))

  private fun axisOut(forward: Boolean): ExitTransition =
    slideOutHorizontally(
      animationSpec = tween(Motion.MEDIUM, easing = Motion.EmphasizedAccelerate),
      targetOffsetX = { if (forward) -axisOffsetPx else axisOffsetPx }
    ) + fadeOut(tween(FADE_OUT_MS, easing = LinearEasing))

  private fun fadeThroughIn(): EnterTransition =
    fadeIn(tween(FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = Motion.EmphasizedDecelerate)) +
      scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(FADE_IN_MS, delayMillis = FADE_OUT_MS, easing = Motion.EmphasizedDecelerate)
      )

  private fun fadeThroughOut(): ExitTransition =
    fadeOut(tween(FADE_OUT_MS, easing = Motion.EmphasizedAccelerate))

  private companion object {
    // Fade-through e shared axis do M3: quem sai some no primeiro terco, quem
    // entra aparece nos dois tercos restantes - nunca se sobrepoem no meio.
    const val FADE_OUT_MS = Motion.MEDIUM / 3
    const val FADE_IN_MS = Motion.MEDIUM - FADE_OUT_MS
  }
}

private val tabRoutes = TopLevelDestination.values().map { it.route }.toSet()

private fun NavScope.isBetweenTabs(): Boolean =
  initialState.destination.route in tabRoutes && targetState.destination.route in tabRoutes

@Composable
fun rememberNavTransitions(): NavTransitions {
  val reducedMotion = LocalReducedMotion.current
  // 30dp e o deslocamento do shared axis no M3: o bastante para dar direcao sem
  // arrastar a tela inteira.
  val offsetPx = with(LocalDensity.current) { 30.dp.roundToPx() }
  return remember(reducedMotion, offsetPx) { NavTransitions(reducedMotion, offsetPx) }
}

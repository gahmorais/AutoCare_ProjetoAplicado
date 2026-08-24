package br.com.gabrielmorais.autocare.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta "painel de instrumentos". As tres cores de estado da manutencao vem das
 * luzes de advertencia do carro e caem em papeis do M3 sem precisar de extensao
 * de tema:
 *
 * - em dia / concluida -> secondary (oliva)
 * - vence em breve     -> tertiary  (ambar)
 * - vencida            -> error     (ferrugem)
 *
 * Ferrugem e nao vermelho puro de propósito: uma manutencao atrasada e uma
 * pendencia do usuario, nao uma falha do sistema.
 */

// --- Claro -------------------------------------------------------------------
val PetroleoClaro = Color(0xFF0F3B47)
val PetroleoContainerClaro = Color(0xFFBFE0E8)
val PetroleoOnContainerClaro = Color(0xFF062730)

val OlivaClaro = Color(0xFF6B8E4E)
val OlivaContainerClaro = Color(0xFFD7E8C4)
val OlivaOnContainerClaro = Color(0xFF24350F)

val AmbarClaro = Color(0xFFE8A33D)
val AmbarContainerClaro = Color(0xFFFFE0B0)
val AmbarOnContainerClaro = Color(0xFF2A1800)

val FerrugemClaro = Color(0xFFC2452D)
val FerrugemContainerClaro = Color(0xFFFFDAD3)
val FerrugemOnContainerClaro = Color(0xFF410900)

// Neutros com vies de teal - escolhidos, nao herdados de um cinza puro.
val ConcretoClaro = Color(0xFFEFF2F2)
val SuperficieClara = Color(0xFFFFFFFF)
val SuperficieVarianteClara = Color(0xFFDDE5E6)
val TintaClara = Color(0xFF0B1A1F)
val TintaSuaveClara = Color(0xFF55676C)
val ContornoClaro = Color(0xFF7E9296)
val ContornoSuaveClaro = Color(0xFFCFD9DA)

// Superficie invertida: o snackbar vive nela. Sem estes papeis definidos o M3
// cai no baseline e o rotulo da acao sai roxo - o mesmo roxo do template que a
// migracao tirou de todo o resto.
val SuperficieInvertidaClara = Color(0xFF1B2A2F)
val TintaInvertidaClara = Color(0xFFEFF2F2)
val PetroleoInvertidoClaro = Color(0xFF7FBECD)

// --- Escuro ------------------------------------------------------------------
// O petroleo #0F3B47 perde contraste sobre fundo escuro, entao clareia.
val PetroleoEscuro = Color(0xFF5FA3B4)
val PetroleoContainerEscuro = Color(0xFF1B5A6B)
val PetroleoOnEscuro = Color(0xFF06171D)

val OlivaEscuro = Color(0xFF9CBE7C)
val OlivaContainerEscuro = Color(0xFF3F5A2E)
val OlivaOnEscuro = Color(0xFF17280A)

val AmbarEscuro = Color(0xFFF0B75E)
val AmbarContainerEscuro = Color(0xFF6B4A12)
val AmbarOnEscuro = Color(0xFF3A2400)

val FerrugemEscuro = Color(0xFFE4705A)
val FerrugemContainerEscuro = Color(0xFF8C2C18)
val FerrugemOnEscuro = Color(0xFF4A0E05)

val ConcretoEscuro = Color(0xFF0D1417)
val SuperficieEscura = Color(0xFF131E22)
val SuperficieVarianteEscura = Color(0xFF2A3A42)
val TintaEscura = Color(0xFFE6EDED)
val TintaSuaveEscura = Color(0xFF93A6AB)
val ContornoEscuro = Color(0xFF6B8085)
val ContornoSuaveEscuro = Color(0xFF2A3A42)

val SuperficieInvertidaEscura = Color(0xFFE6EDED)
val TintaInvertidaEscura = Color(0xFF0B1A1F)
val PetroleoInvertidoEscuro = Color(0xFF0F3B47)

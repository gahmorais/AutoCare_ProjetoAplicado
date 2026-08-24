package br.com.gabrielmorais.autocare.data.images

/**
 * Injeta transformacoes na URL do Cloudinary para que a imagem chegue no tamanho
 * em que sera exibida.
 *
 * Sem isso o app baixa o original de ate 1600px para desenhar em 100dp. A
 * transformacao entra logo apos `/image/upload/`:
 *
 *     https://res.cloudinary.com/c/image/upload/v1/abc.jpg
 *     https://res.cloudinary.com/c/image/upload/c_fill,w_400,h_400/v1/abc.jpg
 *
 * Funcao pura sobre String de proposito: e o unico pedaco da camada de imagem que
 * da para cobrir por teste JVM, entao vale mante-lo assim.
 */
object CloudinaryUrl {

  private const val CLOUDINARY_HOST = "res.cloudinary.com"
  private const val UPLOAD_MARKER = "/image/upload/"

  /** `v1234567890` - o segmento de versao, que nao e transformacao. */
  private val VERSION_SEGMENT = Regex("^v\\d+$")

  /** `c_fill`, `w_400`, `f_auto`... - prefixo curto seguido de underscore. */
  private val TRANSFORMATION_SEGMENT = Regex("^[a-z]{1,3}_[^/]+")

  /**
   * @return a URL com [transformation] aplicada, ou a entrada intacta se ela nao
   * for do Cloudinary (URLs legadas do Firebase Storage passam direto) ou se ja
   * carregar uma transformacao.
   */
  fun withTransformation(url: String?, transformation: String): String? {
    if (url.isNullOrBlank()) return url
    if (!url.contains(CLOUDINARY_HOST)) return url

    val markerIndex = url.indexOf(UPLOAD_MARKER)
    if (markerIndex < 0) return url

    val pathStart = markerIndex + UPLOAD_MARKER.length
    val remainder = url.substring(pathStart)
    if (remainder.isEmpty()) return url

    // Nao empilha transformacao sobre transformacao.
    val firstSegment = remainder.substringBefore('/')
    if (!VERSION_SEGMENT.matches(firstSegment) &&
      TRANSFORMATION_SEGMENT.containsMatchIn(firstSegment)
    ) {
      return url
    }

    return url.substring(0, pathStart) + transformation + "/" + remainder
  }
}

/**
 * `f_auto` serve WebP/AVIF conforme o aparelho e `q_auto` ajusta a compressao,
 * ambos decididos pelo Cloudinary.
 */
object CloudinaryTransformations {
  /** Foto de perfil do drawer, exibida em 120dp. */
  const val AVATAR = "c_fill,w_400,h_400,f_auto,q_auto"

  /** Miniatura do card na lista de veiculos, exibida em 100dp. */
  const val VEHICLE_THUMBNAIL = "c_fill,w_300,h_300,f_auto,q_auto"

  /** Banner da tela de detalhes, ocupa a largura toda. */
  const val VEHICLE_BANNER = "c_limit,w_1000,f_auto,q_auto"
}

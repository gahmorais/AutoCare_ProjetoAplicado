package br.com.gabrielmorais.autocare.data.images

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class CloudinaryUrlTest {

  private val transformation = "c_fill,w_400,h_400,f_auto,q_auto"

  @Test
  fun `injeta a transformacao logo apos o marcador de upload`() {
    val url = "https://res.cloudinary.com/dwmwjnryp/image/upload/v1699999999/autocare/abc.jpg"

    assertEquals(
      "https://res.cloudinary.com/dwmwjnryp/image/upload/" +
        "c_fill,w_400,h_400,f_auto,q_auto/v1699999999/autocare/abc.jpg",
      CloudinaryUrl.withTransformation(url, transformation)
    )
  }

  @Test
  fun `preserva a pasta quando nao ha segmento de versao`() {
    val url = "https://res.cloudinary.com/dwmwjnryp/image/upload/autocare/abc.jpg"

    assertEquals(
      "https://res.cloudinary.com/dwmwjnryp/image/upload/" +
        "c_fill,w_400,h_400,f_auto,q_auto/autocare/abc.jpg",
      CloudinaryUrl.withTransformation(url, transformation)
    )
  }

  // URLs legadas continuam no banco ate o script de limpeza rodar; mexer nelas so
  // produziria uma URL invalida diferente.
  @Test
  fun `deixa url do firebase storage intacta`() {
    val url = "https://firebasestorage.googleapis.com/v0/b/autocare/o/foto.jpg?alt=media"

    assertEquals(url, CloudinaryUrl.withTransformation(url, transformation))
  }

  @Test
  fun `deixa url de host desconhecido intacta`() {
    val url = "https://exemplo.com/image/upload/v1/foto.jpg"

    assertEquals(url, CloudinaryUrl.withTransformation(url, transformation))
  }

  @Test
  fun `nao empilha transformacao sobre transformacao`() {
    val url = "https://res.cloudinary.com/dwmwjnryp/image/upload/" +
      "c_limit,w_1000/v1699999999/autocare/abc.jpg"

    assertEquals(url, CloudinaryUrl.withTransformation(url, transformation))
  }

  @Test
  fun `trata nulo e vazio sem quebrar`() {
    assertNull(CloudinaryUrl.withTransformation(null, transformation))
    assertEquals("", CloudinaryUrl.withTransformation("", transformation))
  }

  @Test
  fun `deixa intacta url do cloudinary sem o marcador de upload`() {
    val url = "https://res.cloudinary.com/dwmwjnryp/raw/authenticated/v1/arquivo.pdf"

    assertEquals(url, CloudinaryUrl.withTransformation(url, transformation))
  }

  // 'v1' sozinho e versao, nao transformacao - o regex de versao precisa vencer.
  @Test
  fun `reconhece versao curta como versao e nao como transformacao`() {
    val url = "https://res.cloudinary.com/dwmwjnryp/image/upload/v1/abc.jpg"

    assertEquals(
      "https://res.cloudinary.com/dwmwjnryp/image/upload/" +
        "c_fill,w_400,h_400,f_auto,q_auto/v1/abc.jpg",
      CloudinaryUrl.withTransformation(url, transformation)
    )
  }
}

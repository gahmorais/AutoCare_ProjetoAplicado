package br.com.gabrielmorais.autocare.data.images

import android.net.Uri

/**
 * Envia uma imagem e devolve a URL publica dela.
 *
 * A abstracao existe por um motivo concreto: o upload nao assinado do Cloudinary
 * carrega o upload preset dentro do APK, de onde ele pode ser extraido. Se um dia
 * houver um backend capaz de assinar as requisicoes, so esta implementacao muda -
 * os repositorios e as ViewModels ficam intactos.
 */
interface ImageUploader {
  /**
   * @param image conteudo local (content:// ou file://) escolhido pelo usuario.
   * @return a URL https publica da imagem hospedada.
   * @throws java.io.IOException se a leitura local ou o envio falharem.
   */
  suspend fun upload(image: Uri): String
}

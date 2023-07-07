package br.com.gabrielmorais.autocare.data.repository.services

import br.com.gabrielmorais.autocare.data.models.Service

interface ServicesRepository {
  fun getServices(onSuccess: (List<Service?>) -> Unit, onError: (Throwable) -> Unit)
}
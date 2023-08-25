package br.com.gabrielmorais.autocare.sampleData

import br.com.gabrielmorais.autocare.R
import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import java.time.LocalDate
import java.time.Month

val maintenanceListSample = MutableList(10) {
  Maintenance(
    description = "Troca de óleo",
    date = LocalDate.of(2023, Month.FEBRUARY, 25).toEpochDay(),
    currentMileage = 75000,
    forecastNextExchangeMileage = 82000,
    forecastNextExchangeDate = LocalDate.of(2023, Month.SEPTEMBER, 30).toEpochDay(),
    comments = "Óleo ELF 10W40 Semisintético"
  )
}.toList()

val vehicleSample = Vehicle(
  nickName = "Meu Carro",
  brand = "Renault",
  model = "Sandero",
  plate = "XXX1234", //URL_VEHICLE_SAMPLE,
  maintenances = maintenanceListSample
)


val userSample = User(
  photo = R.drawable.profile.toString(),
  email = "teste@teste.com.br",
  name = "Gabriel Morais",
  vehicles = listOf(vehicleSample)
)



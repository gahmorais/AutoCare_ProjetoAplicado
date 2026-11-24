package br.com.gabrielmorais.autocare.sampleData

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.Vehicle
import java.time.LocalDate
import java.time.Month

val maintenanceListSample = MutableList(10) { index ->
  Maintenance(
    description = "Troca de óleo",
    date = LocalDate.of(2023, Month.FEBRUARY, 25).toEpochDay(),
    currentMileage = 75000,
    forecastNextExchangeMileage = 82000,
    forecastNextExchangeDate = LocalDate.of(2023, Month.SEPTEMBER, 30).toEpochDay(),
    comments = "Óleo ELF 10W40 Semisintético",
    // Alternado para os previews mostrarem os dois estados do card.
    completed = index % 2 == 0
  )
}.toList()

val vehicleSample = Vehicle(
  nickName = "Meu Carro",
  brand = "Renault",
  model = "Sandero",
  plate = "XXX1234",
  maintenances = maintenanceListSample
)

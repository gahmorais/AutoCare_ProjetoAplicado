package br.com.gabrielmorais.autocare.sampleData

import br.com.gabrielmorais.autocare.data.models.Maintenance
import br.com.gabrielmorais.autocare.data.models.User
import br.com.gabrielmorais.autocare.data.models.Vehicle
import java.time.LocalDate
import java.time.Month

private const val URL_VEHICLE_SAMPLE =
  "https://quatrorodas.abril.com.br/wp-content/uploads/2022/10/stepway10_001-1-e1665169601442.jpg?quality=70&strip=info&w=1280&h=720&crop=1"

private const val URL_USER_PROFILE_SAMPLE =
  "https://static.catapult.co/cdn-cgi/image/width=1170,height=658,dpr=2,fit=cover,format=auto/production/stories/31705/cover_photos/original/iron_man_site_1633028435_1637683340.jpg"

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
  photo = URL_USER_PROFILE_SAMPLE,
  email = "teste@teste.com.br",
  name = "Gabriel Morais",
  vehicles = listOf(vehicleSample)
)



package com.example.app

import com.example.app.models._
import org.joda.time.Duration

import scala.concurrent.Future
import Tables._
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

object DataImport {

  def insertCityData(cityName: String, countryName: String, db: Database) = {
    val city = db.run(getCity(cityName, countryName)).map(_.map(c => City.reify(c))).flatMap{
      case Some(k) => Future.apply(k)
      case None => City.save(City(cityName, countryName, 0))
    }

    city.foreach(c => deleteActivitiesBy(c, db))

    val activities = getActivityDataFromTxt(txtName(cityName, countryName))

    val insertedActivities = Activity.saveMany(activities)

    val cityActivityConnections = for {
      c <- city
      ia <- insertedActivities
    } yield ia.map(i => CityActivity(c.id, i.id, 0))
    cityActivityConnections.foreach(CityActivity.createMany)
  }


  def optionParseString[A](a: String)(f: String => A) =
    if(a == "None" || a == "" || a == " ")
      None
    else
      Some(f(a))

  val dataPath = "src/main/resources/data/"

  def txtName(cityName: String, countryName: String) =
    dataPath+countryName+"_"+cityName+"_updated.txt"

  def getActivityDataFromTxt(txtLocation: String) = {

    var acts = Seq[Activity]()
    val bufferedSource = io.Source.fromFile(txtLocation)
    for(line <- bufferedSource.getLines) {

      val cols = line.split("\t").map(_.trim).toSeq

      if(cols.size >= 13) {

        val name = cols(2)
        val description = optionParseString(cols(3))(a => a)
        val longitude = optionParseString(cols(5))(a => a.toDouble)
        val latitude = optionParseString(cols(6))(a => a.toDouble)
        val duration = optionParseString(cols(4))(a => a.toInt)
        val funRating = optionParseString(cols(7))(a => a.toDouble)
        val hours = optionParseString(cols(14))(a => a.replaceAll("\\[|\\]|\\'", ""))
        val activityType = cols(17).trim
        if(Seq(longitude, latitude, funRating) forall(_.isDefined))
          acts = acts :+ Activity(name, description, Location(longitude.get, latitude.get), duration.map(d => new Duration(d * 60 * 1000)), funRating.get, hours, ActivityType.fromString(activityType), 0)

      }
    }
    bufferedSource.close
    acts
  }

  def deleteActivitiesBy(city: City, db: Database) = {
    db.run(Tables.cityActivities.filter(_.cityId === city.id).result).map(cacs => {
      CityActivity.deleteMany(cacs.map(x => x._1))
      Activity.deleteMany(cacs.map(_._3))
    })
  }

  def getCity(cityName: String, countryName: String) = {
    cities.filter(c => c.name === cityName && c.country === countryName).result.headOption
  }

}

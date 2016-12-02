package com.example.app

import com.example.app.models.{Activity, City, Location}
import org.joda.time.Duration
import scala.concurrent.Future
import Tables._
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

object DataImport {

  def insertCityData(cityName: String, countryName: String, db: Database) = {
    val city = db.run(getCity(cityName, countryName)).map(_.map(c => City.reify(c))).flatMap{
      case Some(k) => Future.apply(k)
      case None => insertCity(cityName, countryName, db)
    }

    city.foreach(c => println("NAME: "+c.name+" ID: "+c.id))

    city.foreach(c => deleteActivitiesBy(c, db))

    val activities = getActivityDataFromTxt(txtName(cityName, countryName))

    val insertedActivities = insertActivities(activities, db)

    val cityActivityConnections = for {
      c <- city
      ia <- insertedActivities
    } yield ia.map(i => (c, i))
    cityActivityConnections.foreach(ca => insertCityActivityConnections(ca, db))
  }


  def optionParseString[A](a: String)(f: String => A) =
    if(a == "None" || a == "" || a == " ")
      None
    else
      Some(f(a))

  val dataPath = "src/main/resources/data/"

  def txtName(cityName: String, countryName: String) =
    dataPath+countryName+"_"+cityName+"_updated.txt"

  def insertCityActivityConnections(connections: Seq[(City, Activity)], db: Database) = {
    db.run(Tables.cityActivities ++= connections.map(c => (0, c._1.id, c._2.id)))
  }

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
        val funRating = optionParseString(cols(13))(a => a.toDouble)
        if(Seq(longitude, latitude, duration, funRating) forall(_.isDefined))
          acts = acts :+ Activity(name, description, Location(longitude.get, latitude.get), new Duration(duration.get * 60 * 1000), funRating.get, Nil, 0)

      }
    }
    bufferedSource.close
    acts
  }

  def deleteActivitiesBy(city: City, db: Database) = {
    db.run(Tables.cityActivities.filter(_.cityId === city.id).result).map(cacs => {
      db.run(Tables.cityActivities.filter(_.id inSet cacs.map(x => x._1)).delete)
      db.run(Tables.activities.filter(_.id inSet cacs.map(x => x._3)).delete)
    })
  }

  def insertActivities(activities: Seq[Activity], db: Database) = {

    val insertable = activities.map(a => (a.id, a.name, a.description, a.location.longitude, a.location.latitude, a.duration.getStandardMinutes.toInt, a.funRating, ""))
    val inserted = db.run((Tables.activities returning Tables.activities.map(_.id)) ++= insertable)
    inserted.onSuccess{ case a => println("INSERTED THIS MANY: "+a.size)}
    inserted.onFailure{case _ => println("NOPE")}
    inserted.map(_.zipWithIndex.map({case (id, index) => activities(index).copy(id = id)}))
  }

  def insertCity(cityName: String, countryName: String, db: Database) = {
    db.run((Tables.cities returning Tables.cities.map(_.id)) += (0, cityName, countryName))
      .map(id => City(cityName, id))
  }

  def getCity(cityName: String, countryName: String) = {
    cities.filter(c => c.name === cityName && c.country === countryName).result.headOption
  }




}

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
    dataPath+countryName+"_"+cityName+"_download.txt"

  val TRUE = "true"

  def getActivityDataFromTxt(txtLocation: String) = {

    var acts = Seq[Activity]()
    val bufferedSource = io.Source.fromFile(txtLocation)
    for(line <- bufferedSource.getLines) {

      val cols = line.split("\t").map(_.trim).toSeq

      if(cols.size >= 13) {
        val name = cols(0) //YES
        val eventType = cols(3).trim //YES
        val summary = optionParseString(cols(4))(a => a) //YES
        val description = optionParseString(cols(5))(a => a) //NEW
        val openHoursString = optionParseString(cols(6))(a => a) //NEW
        val openHoursTimes = optionParseString(cols(7))(a => a.replaceAll("\\[|\\]|\\'", "")) //NO
        val photo = optionParseString(cols(8))(a => a) //NEW
        val link = optionParseString(cols(9))(a => a) //NEW
        val lonelyPlanetTopPick = optionParseString(cols(10))(a => a.toLowerCase == TRUE) //NO
        val tripAdvisorTopChoice = optionParseString(cols(11))(a => a.toLowerCase == TRUE) //NO
        val calcedDuration = optionParseString(cols(12))(a => a.toInt) //YES
        val priceString = optionParseString(cols(13))(a => a) //NEW
        val lonelyPlanetSubTypes = (optionParseString(cols(14))(a => a.replaceAll("\\[|\\]|\\'", "").split(",").toSeq)).getOrElse(Nil) //NO
        val tripAdvisorSubTypes = (optionParseString(cols(15))(a => a.replaceAll("\\[|\\]|\\'", "").split(",").toSeq)).getOrElse(Nil) //NEW
        val latitude = optionParseString(cols(16))(a => a.toDouble) //YES
        val longitude = optionParseString(cols(17))(a => a.toDouble) //YES
        val overallScore = optionParseString(cols(18))(a => a.toDouble) //YES
        val tripAdvisorScore = optionParseString(cols(19))(a => a.toDouble) //NO
        val tripAdvisorRatings = (optionParseString(cols(20))(a => {
          val stringArray = a.replaceAll("\\[|\\]|\\'", "").split(",").toSeq
          if(stringArray.size == 5)
            stringArray.map(_.trim.toInt)
          else
            Nil
        })).getOrElse(Nil) //NO
        val lonelyPlanetStreetAddress = optionParseString(cols(21))(a => a) //CALC
        val tripAdvisorStreetAddress = optionParseString(cols(22))(a => a)  //CALC
        val tripAdvisorCertificateOfExcellence = optionParseString(cols(23))(a => a.toLowerCase == TRUE) //NO
        val tripAdvisorRank = optionParseString(cols(24))(a => a.toInt) //NO
        val tripAdvisorDuration = optionParseString(cols(25))(a => a.toInt) //NO
        val calcedOpenHoursTimes = optionParseString(cols(26))(a => a.replaceAll("\\[|\\]|\\'", "")) //YES

/*        val name = cols(0)
        val description = optionParseString(cols(3))(a => a)
        val longitude = optionParseString(cols(5))(a => a.toDouble)
        val latitude = optionParseString(cols(6))(a => a.toDouble)
        val duration = optionParseString(cols(4))(a => a.toInt)
        val funRating = optionParseString(cols(7))(a => a.toDouble)
        val hours = optionParseString(cols(14))(a => a.replaceAll("\\[|\\]|\\'", ""))
        val activityType = cols(17).trim*/
        val address = if(tripAdvisorStreetAddress.isDefined)
          tripAdvisorStreetAddress
        else
          lonelyPlanetStreetAddress

        if(Seq(longitude, latitude, overallScore) forall(_.isDefined))
          acts = acts :+ Activity(name, summary, description, Location(longitude.get, latitude.get),
            calcedDuration.map(d => new Duration(d * 60 * 1000)), overallScore.get, calcedOpenHoursTimes,
            ActivityType.fromString(eventType), openHoursString, photo, link, priceString, tripAdvisorSubTypes, address, 0)

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

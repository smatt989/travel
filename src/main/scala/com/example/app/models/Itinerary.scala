package com.example.app.models

import java.sql.Timestamp

import com.example.app.{HasIntId, Updatable}
import org.joda.time.{DateTime, Duration, Interval}

import scala.concurrent.ExecutionContext.Implicits.global
import com.example.app.Tables
import com.example.app.Tables.Itineraries
import slick.lifted.TableQuery
import slick.driver.H2Driver.api._

import scala.concurrent.Future

case class Itinerary(cityId: Int, startDateTime: DateTime, endDateTime: DateTime, id: Int) extends HasIntId[Itinerary]{
  def scheduledActivities: Seq[ScheduledActivity] = ???
  def desiredActivities: Set[Activity] = ???
  def rejectedActivities: Future[Set[Activity]] = ???
  lazy val city = City.byId(cityId)
  lazy val interval = new Interval(startDateTime, endDateTime)

  def updateId(id: Int) = this.copy(id = id)

  lazy val toJson =
    city.map(c => JsonItinerary(c, startDateTime.getMillis, endDateTime.getMillis, id))
}

object Itinerary extends Updatable[Itinerary, (Int, Int, Timestamp, Timestamp), Tables.Itineraries]{
  def updateQuery(a: Itinerary) = table.filter(_.id === a.id)
      .map(x => (x.cityId, x.startDateTime, x.endDateTime))
        .update(a.cityId, new Timestamp(a.startDateTime.getMillis), new Timestamp(a.endDateTime.getMillis))

  lazy val table: TableQuery[Itineraries] = Tables.itineraries

  def reify(tuple: (Int, Int, Timestamp, Timestamp)): Itinerary =
    Itinerary(tuple._2, new DateTime(tuple._3), new DateTime(tuple._4), tuple._1)

  def classToTuple(a: Itinerary): (Int, Int, Timestamp, Timestamp) =
    (a.id, a.cityId, new Timestamp(a.startDateTime.getMillis), new Timestamp(a.endDateTime.getMillis))
}

case class JsonItinerary(city: City, startDateTime: Long, endDateTime: Long, id: Int)

case class ItineraryForm(cityId: Int, startDateTime: Long, endDateTime: Long, id: Int = 0){
  def toModel = Itinerary(cityId, new DateTime(startDateTime), new DateTime(endDateTime), id)
}

case class DayOpeningHours(openTime: DateTime, closeTime: DateTime) {
  require {openTime.getMillis <= closeTime.getMillis}
  require {closeTime.getMillis <= DayOpeningHours.endOfFullWeek.getMillis}

  lazy val interval: Interval = new Interval(openTime, closeTime)
}

object DayOpeningHours {
  val endOfFullWeek = (new DateTime(0)).plus(Duration.standardDays(7))
}


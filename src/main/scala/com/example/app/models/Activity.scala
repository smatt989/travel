package com.example.app.models

import com.example.app.{HasIntId, Tables, Updatable}
import org.joda.time.{DateTime, Duration, Interval}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class Activity(name: String,
                    description: Option[String],
                    location: Location,
                    duration: Option[Duration],
                    funRating: Double,
                    openingHours: Option[String],
                    activityType: ActivityType,
                    id: Int) extends HasIntId[Activity]{

  lazy val weekOpeningHours: Option[Seq[DayOpeningHours]] = openingHours.map(_.split(",").toSeq.map(s => {
    val Seq(startHours, startMinutes, endHours, endMinutes) =
      s.trim.split("-").toSeq.map(_.trim).flatMap(_.split(":").toSeq.map(_.toInt))
    DayOpeningHours(
      new DateTime(0).plusHours(startHours).plusMinutes(startMinutes),
      new DateTime(0).plusHours(endHours).plusMinutes(endMinutes)
    )
  }))

  def updateId(id: Int) = this.copy(id = id)

  //maybe needs to be tri-value logic (include unknown)
  def isOpen(during: Interval): Option[Boolean] =
    Activity.isOpen(this, during)

  def canStart(at: DateTime): Option[Boolean] =
    duration.flatMap(d => isOpen(new Interval(at, at.plus(d))))

  def toJson = JsonActivity(name, description, location, duration.map(_.getStandardMinutes.toInt), funRating, openingHours, activityType.name, id)
}

case class ActivityType(name: String)

object ActivityType {
  val hotel = ActivityType("hotel")
  val restaurant = ActivityType("restaurant")
  val activity = ActivityType("activity")

  def fromString(s: String) =
    s match {
      case "hotel" => hotel
      case "restaurant" => restaurant
      case "activity" => activity
    }
}

case class JsonActivity(name: String,
                        description: Option[String],
                        location: Location,
                        duration: Option[Int],
                        funRating: Double,
                        openingHours: Option[String],
                        activityType: String,
                        id: Int)

object Activity extends Updatable[Activity, (Int, String, Option[String], Double, Double, Option[Int], Double, Option[String], String), Tables.Activities]{

  val seconds = 1000
  val minutes = 60 * seconds
  val hours = 60 * minutes

  lazy val table = Tables.activities

  def classToTuple(a: Activity) =
    (a.id, a.name, a.description, a.location.longitude, a.location.latitude, a.duration.map(_.getStandardMinutes.toInt), a.funRating, a.openingHours, a.activityType.name)

  def updateQuery(a: Activity) =
    table.filter(_.id === a.id)
      .map(x => (x.name, x.description, x.longitude, x.latitude, x.duration, x.funRating, x.weekOpeningHours, x.activityType))
      .update((a.name, a.description, a.location.longitude, a.location.latitude, a.duration.map(_.getStandardMinutes.toInt), a.funRating, a.openingHours, a.activityType.name))

  def reify(tuple: (Int, String, Option[String], Double, Double, Option[Int], Double, Option[String], String)) =
    Activity(tuple._2, tuple._3, Location(tuple._4, tuple._5), tuple._6.map(a => new Duration(a * 60 * 1000)), tuple._7, tuple._8, ActivityType.fromString(tuple._9), tuple._1)

  def isOpen(activity: Activity, during: Interval): Option[Boolean] = {
    if(activity.weekOpeningHours.isDefined) {
      val allCoveredMinutes = activity.weekOpeningHours.get.foldLeft(Set[Int]())((a, b) => a ++ intervalToSetOfMinutesOfWeek(b.interval))
      val duringMinutes = intervalToSetOfMinutesOfWeek(during)
      Some(duringMinutes.diff(allCoveredMinutes) == Set.empty[Int])
    }
    else
      None
  }

  def intervalToSetOfMinutesOfWeek(interval: Interval): Set[Int] = {
    val start = dateTimeToMinuteOfNormalizedWeek(interval.getStart)
    val end = dateTimeToMinuteOfNormalizedWeek(interval.getEnd)
    if(start > end)
      (start to lastMinute).toSet ++ (0 to end).toSet
    else
      (start to end).toSet
  }

  def dateTimeToMinuteOfNormalizedWeek(dateTime: DateTime): Int = {
    val dayOfWeek = dateTime.getDayOfWeek
    val minute = dateTime.getMinuteOfDay
    dayOfWeek * minutesPerDay + minute
  }

  lazy val minutesPerDay = 24 * 60
  lazy val lastMinute: Int = 7 * minutesPerDay
}
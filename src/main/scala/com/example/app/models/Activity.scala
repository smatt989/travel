package com.example.app.models

import com.example.app.{HasIntId, Tables, Updatable}
import org.joda.time.{DateTime, Duration, Interval}
import org.slf4j.LoggerFactory
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

case class Activity(name: String,
                    summary: Option[String],
                    description: Option[String],
                    location: Location,
                    duration: Option[Duration],
                    funRating: Double,
                    openingHours: Option[String],
                    activityType: ActivityType,
                    openingHoursText: Option[String],
                    photoUrl: Option[String],
                    linkUrl: Option[String],
                    priceText: Option[String],
                    subTypes: Seq[String],
                    addressText: Option[String],
                    id: Int) extends HasIntId[Activity]{

  lazy val weekOpeningHoursRanges = {
    openingHours match {
      case Some(st) if st.trim != "" => Some(
        st.split(",").toSeq.map(s => {
          val Seq(startHours, startMinutes, endHours, endMinutes) = {
            s.trim.split("-").toSeq.map(_.trim).flatMap(_.split(":").toSeq.map(_.toInt))
          }
          HourRange(startHours, startMinutes, endHours, endMinutes)
        }))
      case _ => None
    }

  }

  lazy val weekOpeningHours: Option[Seq[DayOpeningHours]] = weekOpeningHoursRanges.map(_.map(wo =>
    DayOpeningHours(
      new DateTime(0).plusHours(wo.startHours).plusMinutes(wo.startMinutes),
      new DateTime(0).plusHours(wo.endHours).plusMinutes(wo.endMinutes)
    )
  ))

  def updateId(id: Int) = this.copy(id = id)

  //maybe needs to be tri-value logic (include unknown)
  def isOpen(during: Interval): Option[Boolean] =
    Activity.isOpen(this, during)

  def canStart(at: DateTime): Option[Boolean] =
    duration.flatMap(d => isOpen(new Interval(at, at.plus(d))))
}

case class HourRange(startHours: Int, startMinutes: Int, endHours: Int, endMinutes: Int)

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

object Activity extends Updatable[Activity, (Int, String, Option[String], Option[String], Double, Double, Option[Int], Double, Option[String], String, Option[String], Option[String], Option[String], Option[String], Option[String], Option[String]), Tables.Activities]{

  val seconds = 1000
  val minutes = 60 * seconds
  val hours = 60 * minutes

  lazy val table = Tables.activities

  def classToTuple(a: Activity) =
    (a.id, a.name, a.summary, a.description, a.location.longitude, a.location.latitude,
      a.duration.map(_.getStandardMinutes.toInt), a.funRating, a.openingHours, a.activityType.name,
      a.openingHoursText, a.photoUrl, a.linkUrl, a.priceText, Some(a.subTypes.mkString(",")), a.addressText)

  def updateQuery(a: Activity) =
    table.filter(_.id === a.id)
      .map(x => (x.name, x.description, x.longitude, x.latitude, x.duration, x.funRating, x.weekOpeningHours, x.activityType))
      .update((a.name, a.description, a.location.longitude, a.location.latitude, a.duration.map(_.getStandardMinutes.toInt), a.funRating, a.openingHours, a.activityType.name))

  def reify(tuple: (Int, String, Option[String], Option[String], Double, Double, Option[Int], Double, Option[String], String, Option[String], Option[String], Option[String], Option[String], Option[String], Option[String])) =
    Activity(tuple._2,
      tuple._3,
      tuple._4,
      Location(tuple._5, tuple._6  ),
      tuple._7.map(a => new Duration(a * 60 * 1000)),
      tuple._8,
      tuple._9,
      ActivityType.fromString(tuple._10),
      tuple._11,
      tuple._12,
      tuple._13,
      tuple._14,
      tuple._15.map(_.split(",").toSeq).getOrElse(Nil),
      tuple._16,
      tuple._1)
    //Activity(tuple._2, tuple._3, Location(tuple._4, tuple._5), tuple._6.map(a => new Duration(a * 60 * 1000)), tuple._7, tuple._8, ActivityType.fromString(tuple._9), tuple._1)

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
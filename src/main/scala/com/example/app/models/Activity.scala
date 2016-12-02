package com.example.app.models

import com.example.app.{SlickDbObject, Tables}
import models.DayOpeningHours
import org.joda.time.{DateTime, Duration, Interval}

case class Activity(name: String,
                    description: Option[String],
                    location: Location,
                    duration: Duration,
                    funRating: Double,
                    weekOpeningHours: Seq[DayOpeningHours],
                    id: Int) {

  //maybe needs to be tri-value logic (include unknown)
  def isOpen(during: Interval): Boolean =
  Activity.isOpen(this, during)

  def canStart(at: DateTime): Boolean =
    isOpen(new Interval(at, at.plus(duration)))

  def toJson = JsonActivity(name, description, location, duration.getStandardMinutes.toInt, funRating, id)
}

case class JsonActivity(name: String,
                        description: Option[String],
                        location: Location,
                        duration: Int,
                        funRating: Double,
                        id: Int)

object Activity extends SlickDbObject[Activity, Tables.Activities, (Int, String, Option[String], Double, Double, Int, Double, String)]{

  val seconds = 1000
  val minutes = 60 * seconds
  val hours = 60 * minutes

  lazy val table = Tables.activities

  def reify(tuple: (Int, String, Option[String], Double, Double, Int, Double, String)) =
    Activity(tuple._2, tuple._3, Location(tuple._4, tuple._5), new Duration(tuple._6 * 60 * 1000), tuple._7, Nil, tuple._1)

  def isOpen(activity: Activity, during: Interval): Boolean = {
    val allCoveredMinutes = activity.weekOpeningHours.foldLeft(Set[Int]())((a, b) => a ++ intervalToSetOfMinutesOfWeek(b.interval))
    val duringMinutes = intervalToSetOfMinutesOfWeek(during)
    duringMinutes.diff(allCoveredMinutes) == Set.empty[Int]
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
package models

import com.example.app.models.{Activity, City}
import org.joda.time.{DateTime, Duration, Interval}

abstract class Itinerary {
  def city: City
  def interval: Interval
  def scheduledActivities: Seq[ScheduledActivity]
  def desiredActivities: Set[Activity]
  def rejectedActivities: Set[Activity]
}

case class DayOpeningHours(openTime: DateTime, closeTime: DateTime) {
  require {openTime.getMillis <= closeTime.getMillis}
  require {closeTime.getMillis <= DayOpeningHours.endOfFullWeek.getMillis}

  lazy val interval: Interval = new Interval(openTime, closeTime)
}

object DayOpeningHours {
  val endOfFullWeek = (new DateTime(0)).plus(Duration.standardDays(7))
}

abstract class ScheduledActivity {
  def activity: Activity
  def start: DateTime
  def end: DateTime
  def lockedTime: Boolean

  def scheduledDuration: Duration = new Duration(start, end)
}


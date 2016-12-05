package com.example.app.models

import java.sql.Timestamp

import com.example.app.{HasIntId, SlickDbObject, Tables}
import org.joda.time.DateTime
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ScheduledActivity(itineraryId: Int, activityId: Int, startDateTime: DateTime, endDateTime: DateTime, lockedTime: Boolean, id: Int = 0) extends HasIntId[ScheduledActivity]{

  lazy val itinerary = Itinerary.byId(itineraryId)
  lazy val activity = Activity.byId(activityId)

  def updateId(id: Int) = this.copy(id = id)

  def toJson(optionalActivity: Option[Activity]) = {
    val a = if(optionalActivity.isDefined)
        Future.apply(optionalActivity.get)
      else
        activity
    a.map(act => JsonScheduledActivity(act.toJson, startDateTime.getMillis, endDateTime.getMillis, lockedTime))
  }

}

case class JsonScheduledActivity(activity: JsonActivity, startDateTime: Long, endDateTime: Long, lockedTime: Boolean = false)
case class InputScheduledActivity(activityId: Int, startDateTime: Long, endDateTime: Long, lockedTime: Boolean = false){
  def toScheduledActivity(itineraryId: Int) = ScheduledActivity(itineraryId, activityId, new DateTime(startDateTime), new DateTime(endDateTime), lockedTime)
}

object ScheduledActivity extends SlickDbObject[ScheduledActivity, (Int, Int, Int, Timestamp, Timestamp, Boolean), Tables.ScheduledActivities] {
  def table = Tables.scheduledActivities

  def reify(tuple: (Int, Int, Int, Timestamp, Timestamp, Boolean)) =
    ScheduledActivity(tuple._2, tuple._3, new DateTime(tuple._4), new DateTime(tuple._5), tuple._6, tuple._1)

  def classToTuple(a: ScheduledActivity): (Int, Int, Int, Timestamp, Timestamp, Boolean) =
    (a.id, a.itineraryId, a.activityId, new Timestamp(a.startDateTime.getMillis), new Timestamp(a.endDateTime.getMillis), a.lockedTime)

  def byItineraryId(itineraryId: Int) = {
    db.run(table.filter(_.itineraryId === itineraryId).result)
        .map(_.map(reify))
  }

  def deleteByItineraryIdQuery(itineraryId: Int) =
    table.filter(_.itineraryId === itineraryId).delete

  def saveItineraryScheduledActivities(itineraryId: Int, activities: Seq[ScheduledActivity]) = {
    db.run(DBIO.seq(deleteByItineraryIdQuery(itineraryId), createQuery(activities)).transactionally)
  }

  def jsonFromMany(as: Seq[ScheduledActivity]) = {
    val activityIds = as.map(_.activityId)

    val activitiesById = Activity.byIds(activityIds).map(_.map(a => a.id -> a).toMap)

    activitiesById.flatMap(abi => {
      Future.sequence(as.map(a => a.toJson(abi.get(a.activityId))))
    })
  }

  def jsonFromItineraryId(itineraryId: Int) = {
    val scheduledActivities = ScheduledActivity.byItineraryId(itineraryId)

    scheduledActivities.flatMap(jsonFromMany)
  }


}
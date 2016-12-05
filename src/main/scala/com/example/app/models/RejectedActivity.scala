package com.example.app.models

import com.example.app.{HasIntId, SlickDbObject, Tables}
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


case class RejectedActivity(itineraryId: Int, activityId: Int, id: Int = 0) extends HasIntId[RejectedActivity]{

  def updateId(id: Int) = this.copy(id = id)
}

object RejectedActivity extends SlickDbObject[RejectedActivity, (Int, Int, Int), Tables.RejectedActivities] {
  def table = Tables.rejectedActivities

  def reify(tuple: (Int, Int, Int)): RejectedActivity =
    RejectedActivity(tuple._2, tuple._3, tuple._1)

  def classToTuple(a: RejectedActivity): (Int, Int, Int) =
    (a.id, a.itineraryId, a.activityId)

  def byItineraryId(itineraryId: Int) = {
    db.run(table.filter(_.itineraryId === itineraryId).result)
      .map(_.map(reify))
  }

  def deleteByItineraryIdQuery(itineraryId: Int) =
    table.filter(_.itineraryId === itineraryId).delete

  def saveItineraryRejectedActivities(itineraryId: Int, activities: Seq[RejectedActivity]) = {
    db.run(DBIO.seq(deleteByItineraryIdQuery(itineraryId), createQuery(activities)).transactionally)
  }
}
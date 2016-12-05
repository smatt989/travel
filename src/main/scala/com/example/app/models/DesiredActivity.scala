package com.example.app.models

import com.example.app.{HasIntId, SlickDbObject, Tables}
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

case class DesiredActivity(itineraryId: Int, activityId: Int, id: Int = 0) extends HasIntId[DesiredActivity]{
  def updateId(id: Int) = this.copy(id = id)
}

object DesiredActivity extends SlickDbObject[DesiredActivity, (Int, Int, Int), Tables.DesiredActivities] {
  def table = Tables.desiredActivities

  def reify(tuple: (Int, Int, Int)): DesiredActivity =
    DesiredActivity(tuple._2, tuple._3, tuple._1)

  def classToTuple(a: DesiredActivity): (Int, Int, Int) =
    (a.id, a.itineraryId, a.activityId)

  def byItineraryId(itineraryId: Int) = {
    db.run(table.filter(_.itineraryId === itineraryId).result)
      .map(_.map(reify))
  }

  def deleteByItineraryIdQuery(itineraryId: Int) =
    table.filter(_.itineraryId === itineraryId).delete

  def saveItineraryDesiredActivities(itineraryId: Int, activities: Seq[DesiredActivity]) = {
    db.run(DBIO.seq(deleteByItineraryIdQuery(itineraryId), createQuery(activities)).transactionally)
  }
}
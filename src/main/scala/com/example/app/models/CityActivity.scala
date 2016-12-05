package com.example.app.models

import com.example.app.{HasIntId, SlickDbObject, Tables}
import slick.dbio.Effect.Write
import slick.lifted.TableQuery
import slick.profile.FixedSqlAction


case class CityActivity(cityId: Int, activityId: Int, id: Int) extends HasIntId[CityActivity]{
  def updateId(id: Int) = this.copy(id = id)
}

object CityActivity extends SlickDbObject[CityActivity, (Int, Int, Int), Tables.CityActivities]{
  lazy val table = Tables.cityActivities

  def reify(tuple: (Int, Int, Int)): CityActivity =
    CityActivity(tuple._2, tuple._3, tuple._1)

  def classToTuple(a: CityActivity): (Int, Int, Int) =
    (a.id, a.cityId, a.activityId)
}
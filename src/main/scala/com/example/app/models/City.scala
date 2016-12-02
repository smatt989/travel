package com.example.app.models

import com.example.app.{SlickDbObject, Tables}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by matt on 11/27/16.
  */

case class City(name: String, id: Int) {
  def availableActivities =
    City.availableActivities(this)
}

object City extends SlickDbObject[City, Tables.Cities, (Int, String, String)]{

  lazy val table = Tables.cities

  def reify(tuple: (Int, String, String)) =
    City(tuple._2, tuple._1)

  def availableActivities(city: City) =
    availableActivitiesByCityId(city.id)

  def availableActivitiesByCityId(cityId: Int) = db.run((for {
    c <- Tables.cities if c.id === cityId
    a <- Tables.activities
    cacs <- Tables.cityActivities if c.id === cacs.cityId && a.id === cacs.activityId
  } yield a).result).map(_.map(c => Activity.reify(c)))

}
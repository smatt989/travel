package com.example.app

import java.sql.Timestamp

import slick.driver.H2Driver.api._


object Tables {


  class Cities(tag: Tag) extends Table[(Int, String, String)](tag, "CITIES") {
    def id = column[Int]("CITY_ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def country = column[String]("COUNTRY")

    def * = (id, name, country)
  }

  class Activities(tag: Tag) extends Table[(Int, String, Option[String], Double, Double, Int, Double, String)](tag, "ACTIVITIES") {
    def id = column[Int]("ACTIVITY_ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def description = column[Option[String]]("DESCRIPTION")
    def longitude = column[Double]("LONGITUDE")
    def latitude = column[Double]("LATITUDE")
    def duration = column[Int]("DURATION")
    def funRating = column[Double]("FUN_RATING")
    def weekOpeningHours = column[String]("OPENING_HOURS")

    def * = (id, name, description, longitude, latitude, duration, funRating, weekOpeningHours)
  }

  class CityActivities(tag: Tag) extends Table[(Int, Int, Int)](tag, "CITY_ACTIVITIES") {
    def id = column[Int]("CITY_ACTIVITY_ID", O.PrimaryKey, O.AutoInc)
    def cityId = column[Int]("CITY_ID")
    def activityId = column[Int]("ACTIVITY_ID")

    def * = (id, cityId, activityId)

    def city = foreignKey("CITY_ACTIVITIES_TO_CITY_FK", cityId, cities)(_.id)
    def activity = foreignKey("CITY_ACTIVITIES_TO_ACTIVITY_FK", activityId, activities)(_.id)
  }

  class Itineraries(tag: Tag) extends Table[(Int, Int, Timestamp, Timestamp)](tag, "ITINERARIES") {
    def id = column[Int]("ITINERARY_ID", O.PrimaryKey, O.AutoInc)
    def cityId = column[Int]("CITY_ID")
    def startDateTime = column[Timestamp]("START_DATE_TIME")
    def endDateTime = column[Timestamp]("END_DATE_TIME")

    def * = (id, cityId, startDateTime, endDateTime)

    def city = foreignKey("ITINERARIES_TO_CITY_FK", cityId, cities)(_.id)
  }

  class ScheduledActivities(tag: Tag) extends Table[(Int, Int, Int, Timestamp, Timestamp)](tag, "SCHEDULED_ACTIVITIES") {
    def id = column[Int]("SCHEDULED_ACTIVITY_ID", O.PrimaryKey, O.AutoInc)
    def itineraryId = column[Int]("ITINERARY_ID")
    def activityId = column[Int]("ACTIVITY_ID")
    def startDateTime = column[Timestamp]("START_DATE_TIME")
    def endDateTime = column[Timestamp]("START_DATE_TIME")

    def * = (id, itineraryId, activityId, startDateTime, endDateTime)

    def itinerary = foreignKey("SCHEDULED_ACTIVITIES_TO_ITINERARY_FK", itineraryId, itineraries)(_.id)
    def activity = foreignKey("SCHEDULED_ACTIVITIES_TO_ACTIVITY_FK", activityId, activities)(_.id)
  }

  val cities = TableQuery[Cities]
  val itineraries = TableQuery[Itineraries]
  val activities = TableQuery[Activities]
  val cityActivities = TableQuery[CityActivities]
  val scheduledActivities = TableQuery[ScheduledActivities]

  val schemas = (cities.schema ++ itineraries.schema ++ activities.schema ++ cityActivities.schema ++ scheduledActivities.schema)

  // DBIO Action which creates the schema
  val createSchemaAction = schemas.create

  // DBIO Action which drops the schema
  val dropSchemaAction = schemas.drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction)

}


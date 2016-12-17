package com.example.app.Routes

import com.example.app.SlickRoutes
import com.example.app.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ItineraryRoutes extends SlickRoutes{

  post("/itineraries/save") {
    contentType = formats("json")

    val itinerary = parsedBody.extract[ItineraryForm]

    Itinerary.save(itinerary.toModel).flatMap(_.toJson)
  }

  get("/itineraries") {
    contentType = formats("json")

    Itinerary.getAll.flatMap(a => Future.sequence(a.map(_.toJson)))
  }

  get("/itineraries/:id") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt

    Itinerary.byId(itineraryId).flatMap(i => i.toJson)
  }

  get("/itineraries/:id/activities/scheduled") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt

    ScheduledActivity.jsonFromItineraryId(itineraryId)
  }

  post("/itineraries/:id/activities/scheduled/save") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val inputScheduledActivities = parsedBody.extract[List[InputScheduledActivity]]
    val scheduledActivities = inputScheduledActivities.map(_.toScheduledActivity(itineraryId))

    ScheduledActivity.saveItineraryScheduledActivities(itineraryId, scheduledActivities).flatMap(a => {
      ScheduledActivity.jsonFromItineraryId(itineraryId)
    })
  }

  get("/itineraries/:id/activities/desired") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val desiredActivities = DesiredActivity.byItineraryId(itineraryId)
    val activities = desiredActivities.flatMap(da => Activity.byIds(da.map(_.activityId)))

    activities
  }

  post("/itineraries/:id/activities/desired/save") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val inputDesiredActivityIds = parsedBody.extract[List[Int]]
    val desiredActivities = inputDesiredActivityIds.map(id => DesiredActivity(itineraryId, id))

    val activities = DesiredActivity.saveItineraryDesiredActivities(itineraryId, desiredActivities).flatMap(a => {
      val desiredAs = DesiredActivity.byItineraryId(itineraryId)
      desiredAs.flatMap(da => Activity.byIds(da.map(_.activityId)))
    })

    activities
  }

  get("/itineraries/:id/activities/rejected") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val rejectedActivities = RejectedActivity.byItineraryId(itineraryId)
    val activities = rejectedActivities.flatMap(ra => Activity.byIds(ra.map(_.activityId)))

    activities
  }

  post("/itineraries/:id/activities/rejected/save") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val inputRejectedActivityIds = parsedBody.extract[List[Int]]
    val rejectedActivities = inputRejectedActivityIds.map(id => RejectedActivity(itineraryId, id))

    val activities = RejectedActivity.saveItineraryRejectedActivities(itineraryId, rejectedActivities).flatMap(a => {
      val rejectedAs = RejectedActivity.byItineraryId(itineraryId)
      rejectedAs.flatMap(ra => Activity.byIds(ra.map(_.activityId)))
    })

    activities
  }

  post("/itineraries/:id/collaborate") {
    contentType = formats("json")

    val itineraryId = {params("id")}.toInt
    val itinerary = Itinerary.byId(itineraryId)

    val results = {
          itinerary.map(i => {
            new ItineraryGeneration(i).createItinerary()
          })
    }

    results.flatMap(rs => {
      ScheduledActivity.jsonFromMany(rs.filter(_.activityId != 0))
    })

  }

}

package com.example.app.Routes

import com.example.app.SlickRoutes
import com.example.app.models.City
import scala.concurrent.ExecutionContext.Implicits.global

trait CityRoutes extends SlickRoutes{

  case class StringQueryObject(query: String)

  get("/cities") {
    contentType = formats("json")

    City.getAll
  }

  get("/cities/:id/activities") {
    contentType = formats("json")

    val cityId = {params("id")}.toInt

    City.availableActivitiesByCityId(cityId).map(as => {
      as.map(_.toJson)
    })
  }

  post("/cities/search") {
    contentType = formats("json")

    val q = parsedBody.extract[StringQueryObject]

    City.getAll.map(_.filter(_.name contains q.query))
  }


}

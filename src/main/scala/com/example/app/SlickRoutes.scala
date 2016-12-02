package com.example.app

import com.example.app.models.{Activity, City}
import org.scalatra.{CorsSupport, FutureSupport, ScalatraBase}
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra.scalate.ScalateSupport

case class StringQueryObject(query: String)

case class CityCountryObject(cityName: String, countryName: String)

trait SlickRoutes extends ScalatraBase with FutureSupport with JacksonJsonSupport with ScalateSupport with CorsSupport{

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  def db: Database

//  options("/*"){
//    response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
//  }

  get("/db/create-tables") {
    db.run(Tables.createSchemaAction)
  }

  get("/db/drop-tables") {
    db.run(Tables.dropSchemaAction)
  }

  get("/db/load-data") {
    val cities = Seq(("beijing", "china"), ("new-york-city", "usa"), ("chicago", "usa"), ("san-francisco", "usa"))

    for(c <- cities) {
      DataImport.insertCityData(c._1, c._2, db)
    }
  }

  post("/db/update-city"){
    contentType = formats("json")

    val cityCountryObject = parsedBody.extract[CityCountryObject]
    DataImport.insertCityData(cityCountryObject.cityName, cityCountryObject.countryName, db)
    "ok"
  }

  get("/") {
    <html>
      <body>
        <div id="app"></div>
        <script src="/front-end/dist/bundle.js"></script>
      </body>
    </html>
  }

  get("/cities") {
    contentType = formats("json")

    City.getAll
  }

  get("/activities") {
    contentType = formats("json")

    Activity.getAll
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
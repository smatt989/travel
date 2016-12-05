package com.example.app.Routes

import com.example.app.{DataImport, SlickRoutes, Tables}
import com.example.app.models.City
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global


trait DBManagementRoutes extends SlickRoutes{

  get("/db/create-tables") {
    db.run(Tables.createSchemaAction)
  }

  get("/db/drop-tables") {
    db.run(Tables.dropSchemaAction)
  }

  get("/db/load-data") {
    Tables.populateData(db)
  }

  get("/db/reset"){
    db.run(DBIO.seq(Tables.dropSchemaAction, Tables.createSchemaAction)).foreach { a =>
      Tables.populateData(db)
    }
  }

  post("/db/update-city"){
    contentType = formats("json")

    val cityCountryObject = parsedBody.extract[City]
    DataImport.insertCityData(cityCountryObject.name, cityCountryObject.countryName, db)
    "ok"
  }


}

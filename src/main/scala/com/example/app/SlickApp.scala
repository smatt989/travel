package com.example.app

import com.example.app.Routes.{AppRoutes, CityRoutes, DBManagementRoutes, ItineraryRoutes}
import org.scalatra.{FutureSupport, ScalatraServlet}
import slick.driver.H2Driver.api._

/**
  * Created by matt on 11/22/16.
  */

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport
  with AppRoutes
  with CityRoutes
  with ItineraryRoutes
  with DBManagementRoutes{

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}
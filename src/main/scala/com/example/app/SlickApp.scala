package com.example.app

import org.scalatra.{FutureSupport, ScalatraServlet}
import slick.driver.H2Driver.api._

/**
  * Created by matt on 11/22/16.
  */

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}
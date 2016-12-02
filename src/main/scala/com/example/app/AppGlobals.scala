package com.example.app
import slick.driver.H2Driver.api._

object AppGlobals {

  var db: () => Database = null
}

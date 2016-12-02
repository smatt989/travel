package com.example.app

import slick.lifted.TableQuery
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by matt on 12/1/16.
  */
trait SlickDbObject[A, C <: slick.lifted.AbstractTable[_], B] {

  def table: TableQuery[C]

  def reify(tuple: B): A

  def getAll =
    db.run(table.result).map(_.map(a => reify(a.asInstanceOf[B])))

  def db = AppGlobals.db()

}

package de.postgresqlinsideout

import scala.slick.jdbc.meta.MBestRowIdentifierColumn.Scope.Session

/**
 *
 *
 * @author Steffen Hildebrandt
 * @version 14.1
 */
object Main extends App {

  override def main(args: Array[String]) {
    implicit val session = Session
    println(DBAccess.getPageHeader("customers", 0))
  }

}

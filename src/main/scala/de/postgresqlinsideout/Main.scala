package de.postgresqlinsideout


/**
 *
 *
 * @author Steffen Hildebrandt
 * @version 14.1
 */
object Main extends App {

  override def main(args: Array[String]) {
    println(DBAccess.getPageHeader("authors", 0))
    println(DBAccess.getHeapPageItems("authors", 0))
  }

}

package de.postgresqlinsideout.pagelayout

import java.io.File
import de.postgresqlinsideout.pagelayout.data.{Query, Page, DBAccess}
import de.postgresqlinsideout.pagelayout.visualization.PageElement
import de.postgresqlinsideout.pagelayout.visualization.HtmlTable
import scala.slick.session.Database


/**
 * @author Steffen Hildebrandt
 */
object Main extends App {

  override def main(args: Array[String]) {

    //val db = Database.forURL("jdbc:postgresql://localhost/dell", user = "postgres", password = "postgres")
    val db = Database.forURL("jdbc:postgresql://localhost/booktown", user = "postgres", password = "postgres")

    val page = new Page(db, "books", 0)  // other tables for dell: orders, orderlines, cust_hist, products

    page.getPageVisualization.printToFile(new File("output/PageLayout.html"))

    val query = new Query(db, "books", "WHERE author_id > 5000", 0)

    query.getPageVisualization.printToFile(new File("output/QueryLayout.html"))
  }

}

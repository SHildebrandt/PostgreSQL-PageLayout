package de.postgresqlinsideout

import java.io.File
import de.postgresqlinsideout.html.{ContentType, TableItem, HtmlTable}
import de.postgresqlinsideout.data.DBAccess


/**
 * @author Steffen Hildebrandt
 */
object Main extends App {

  override def main(args: Array[String]) {
    val pageHeader = DBAccess.getPageHeader("authors", 0)
    val heapPageItems = DBAccess.getHeapPageItems("authors", 0)
    val table = new HtmlTable()

    import ContentType._
    table.addItem(TableItem(2, 24, HEADER, "Test1"))
    pageHeader.toTableItemList(64) foreach (table.addItem(_))
    //table.addItem(TableItem(28, 258, DATA, "Test2"))
    table.addItem(TableItem(500, 501, HEADER, "TestHeader"))
    table.addItem(TableItem(502, 503, DATA, "Data"))
    table.addItem(TableItem(8010, 8191, DATA, "Data"))
    table.printHTML5(new File("output/PageLayout.html"))
  }

}

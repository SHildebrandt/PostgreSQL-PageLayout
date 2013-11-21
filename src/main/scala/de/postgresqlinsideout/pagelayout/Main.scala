package de.postgresqlinsideout.pagelayout

import java.io.File
import de.postgresqlinsideout.pagelayout.data.DBAccess
import de.postgresqlinsideout.pagelayout.representation.PageElement
import de.postgresqlinsideout.pagelayout.representation.HtmlTable
import de.postgresqlinsideout.pagelayout.representation.ContentType._


/**
 * @author Steffen Hildebrandt
 */
object Main extends App {

  override def main(args: Array[String]) {

    val page = new PageLayout("customers", 0)
    page.getRepresentation.printToFile(new File("output/Authors0.html"))

  }

  def test = {
    val pageHeader = DBAccess.getPageHeader("authors", 0)
    val heapPageItems = DBAccess.getHeapPageItems("authors", 0)
    val table = new HtmlTable()

    println(pageHeader)
    println(heapPageItems)

    table.addItem(PageItem(0, 0, PAGE_HEADER, "StartPos"))
    table.addItem(PageItem(4, 24, PAGE_HEADER, "Test1"))
    pageHeader.toTableItemList(64) foreach (table.addItem(_))
    //table.addItem(TableItem(28, 258, DATA, "Test2"))
    table.addItem(PageItem(500, 501, PAGE_HEADER, "TestHeader"))
    table.addItem(PageItem(502, 503, DATA, "Data"))
    table.addItem(PageItem(8010, 8191, DATA, "Data"))
    table.printToFile(new File("output/PageLayout.html"))
  }

}

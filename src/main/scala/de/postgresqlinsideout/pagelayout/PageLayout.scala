package de.postgresqlinsideout.pagelayout

import de.postgresqlinsideout.pagelayout.representation.{ContentType, PageElement, PageRepresentation, HtmlTable}
import de.postgresqlinsideout.pagelayout.data.DBAccess
import de.postgresqlinsideout.pagelayout.data.Page._


/**
 * This object provides utilities to create a visualization of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class PageLayout(val dbTable: String, val pageNo: Int) {

  private lazy val pageHeader = DBAccess.getPageHeader(dbTable, pageNo)
  private lazy val heapPageItems = DBAccess.getHeapPageItems(dbTable, pageNo)

  def getRepresentation: PageRepresentation = {
    val html = new HtmlTable

    writePageHeaderData(html)
    writeItemIdData(html)
    writeItems(html)

    html
  }

  def writePageHeaderData(page: PageRepresentation) = {
    page.addItem(PageItem(PAGE_HEADER_DATA_START, PAGE_HEADER_DATA_END, ContentType.PAGE_HEADER, pageHeader.toString()))
    // or: pageHeader.toTableItemList(0) foreach (page.addItem(_))
  }

  def writeItemIdData(page: PageRepresentation) = {
    heapPageItems foreach { p =>
      page.addItem(PageItem(p.itemIdDataStart, p.itemIdDataEnd, ContentType.ITEM_ID_DATA, s"Pointer to ${p.firstByte}"))
    }
  }

  def writeItems(page: PageRepresentation) = {
    heapPageItems foreach { p =>
      page.addItem(PageItem(p.firstByte, p.firstByte + DATA_HEADER_SIZE - 1, ContentType.ITEM_HEADER, "Header " + p.lp.value))
      page.addItem(PageItem(p.firstByte + DATA_HEADER_SIZE, p.lastByte, ContentType.DATA, "Entry " + p.lp.value))
    }
  }

}

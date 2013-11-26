package de.postgresqlinsideout.pagelayout.data

import de.postgresqlinsideout.pagelayout.visualization._
import de.postgresqlinsideout.pagelayout.visualization.ItemIdData
import de.postgresqlinsideout.pagelayout.visualization.PageHeader
import de.postgresqlinsideout.pagelayout.visualization.ItemHeader
import de.postgresqlinsideout.pagelayout.visualization.Item
import scala.slick.session.Database

/**
 * A PostgreSQL page and its contents.
 * (cf. http://www.postgresql.org/docs/9.3/static/storage-page-layout.html)
 * Free space and special space are currently ignored.
 *
 * @author Steffen Hildebrandt
 */
class Page(db: Database, table: String, pageNo: Int) {
  import Page._

  private lazy val pageHeaderData = DBAccess.getPageHeader(db, table, pageNo)
  private lazy val heapPageItems = DBAccess.getHeapPageItems(db, table, pageNo)

  def getPageVisualization: PageVisualization = new HtmlTable(pageElements)

  /**
   * Returns all elements in this page as a List of PageElement
   * The Elements are NOT sorted
   * @return a List of PageElements
   */
  lazy val pageElements = {
    val pageHeader = PageHeader(PAGE_HEADER_DATA_START, PAGE_HEADER_DATA_END, pageHeaderData)
    val otherElements = heapPageItems flatMap {hpi =>
      // we have to build "backwards" here to set the pointers
      val item = Item(hpi.firstByte + DATA_HEADER_SIZE, hpi.lastByte, hpi)
      val itemHeader = ItemHeader(hpi.firstByte, hpi.firstByte + DATA_HEADER_SIZE - 1, item)
      val itemIdData = ItemIdData(hpi.itemIdDataStart, hpi.itemIdDataEnd, itemHeader)
      List(itemIdData, itemHeader, item)
    }
    pageHeader :: otherElements
  }


  /* Methods to access the different areas on the page (currently not in use) */

  def getPageHeaderData: PageHeaderData = pageHeaderData

  def getItemIdData: List[ItemIdData] = pageElements flatMap {
    case e : ItemIdData => Some(e)
    case _ => None
  }

  def getItemsWithHeaders: List[(ItemHeader, Item)] = pageElements flatMap {
    case e : ItemHeader => Some(e -> e.item)
    case _ => None
  }

  def getItemsWithoutHeaders: List[Item] = pageElements flatMap {
    case e : Item => Some(e)
    case _ => None
  }

  //def getEmptySpace ...
}

object Page {

  val PAGE_HEADER_DATA_START = 0 // byte number on page
  val PAGE_HEADER_DATA_END = 23 // byte number on page

  val ITEM_ID_DATA_START = 24 // byte number on page

  val DATA_HEADER_SIZE = 23 // bytes
}
package de.postgresqlinsideout.pagelayout.visualization

import de.postgresqlinsideout.pagelayout.data.{DBAccess, PageHeaderData, HeapPageItemData}

/**
 * An element within a page.
 * Objects of this class can be added to a @link{PageVisualization}.
 *
 * @author Steffen Hildebrandt
 */
sealed trait PageElement {
  val firstByte: Int
  val lastByte: Int
  val content: String

  /**
   * Content of this PageElement if in the following rows (if it reaches until there)
   */
  val contentContinued: String

  /**
   * the small box that pops up if you stay on a cell for a bit longer
   */
  def title = content

  val id: String = "id" + firstByte.toString
  val tdClass: String
}

case class PageHeader(firstByte: Int, lastByte: Int, pageHeaderData: PageHeaderData) extends PageElement {
  val content = "PageHeader"
  override def title = pageHeaderData.toString
  val contentContinued = ""
  val tdClass = "pageheader"
}

case class ItemIdData(firstByte: Int, lastByte: Int, itemHeader: ItemHeader) extends PageElement {
  val content = s"--> ${itemHeader.firstByte}"
  override def title = s"Pointer to Byte ${itemHeader.firstByte}"
  val contentContinued = ""
  val tdClass = "itemiddata"
}

case class ItemHeader(firstByte: Int, lastByte: Int, item: Item) extends PageElement {
  val content = "ItemHeader"
  override def title = item.heapPageItem.toString
  val contentContinued = ""
  val tdClass = "itemheader"
}

case class Item(firstByte: Int, lastByte: Int, heapPageItem: HeapPageItemData) extends PageElement {
  val db = heapPageItem.fromDB
  val table = heapPageItem.fromTable
  lazy val content = (db, table) match {
    case (Some(d), Some(t)) => s"${DBAccess.getContentForCtid(d, t, heapPageItem.tCtid.value) mkString ","}"
    case _ => s"Item with ctid = ${heapPageItem.tCtid.value}"
  }
  val contentContinued = "..."
  val tdClass = "item"
}

case class Empty(firstByte: Int, lastByte: Int) extends PageElement {
  val content = ""
  val contentContinued = ""
  override val title = "Empty Space"
  val tdClass = "empty"
}

/**
 * This is actually not a PageElement. It just indicates an ignored range in the Visualization.
 */
case class Ignored(firstByte: Int, lastByte: Int) extends PageElement {
  val content = ""
  val contentContinued = ""
  override val title = "Page content ignored for visualization"
  val tdClass = "ignored"
}

object PageElement {
  val ordering: Ordering[PageElement] = Ordering[Int].on[PageElement](t => t.firstByte)
}
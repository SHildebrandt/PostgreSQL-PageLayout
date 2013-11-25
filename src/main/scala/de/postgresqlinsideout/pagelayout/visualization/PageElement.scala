package de.postgresqlinsideout.pagelayout.visualization

import de.postgresqlinsideout.pagelayout.data.{DBAccess, PageHeaderData, HeapPageItem}

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

  val id: String = "id" + firstByte.toString
  val tdClass: String
}

case class PageHeader(firstByte: Int, lastByte: Int, pageHeaderData: PageHeaderData) extends PageElement {
  val content = pageHeaderData.toString
  val tdClass = "pageheader"
}

case class ItemIdData(firstByte: Int, lastByte: Int, itemHeader: ItemHeader) extends PageElement {
  val content = s"--> ${itemHeader.firstByte}"
  val tdClass = "itemiddata"
}

case class ItemHeader(firstByte: Int, lastByte: Int, item: Item) extends PageElement {
  val content = "ItemHeader"
  val tdClass = "itemheader"
}

case class Item(firstByte: Int, lastByte: Int, item: HeapPageItem) extends PageElement {
  val table = item.fromTable
  lazy val content = table match {
    case Some(t) => s"$t(${DBAccess.getContentForCtid(t, item.tCtid.value) mkString ","})"
    case None => s"Item with ctid = ${item.tCtid.value}"
  }
  val tdClass = "item"
}

case class Empty(firstByte: Int, lastByte: Int) extends PageElement {
  val content = ""
  val tdClass = "empty"
}
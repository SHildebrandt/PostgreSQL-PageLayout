package de.postgresqlinsideout.pagelayout.representation

import de.postgresqlinsideout.pagelayout.data.HeapPageItem

/**
 * An element within a page.
 * Objects of this class can be added to a @link{PageRepresentation}.
 *
 * @author Steffen Hildebrandt
 */
sealed trait PageElement {
  val firstByte: Int
  val lastByte: Int
  val content: String

  val id: String = id + firstByte.toString
  val tdClass: Int
}

case class PageHeader(firstByte: Int, lastByte: Int, content: String) extends PageElement {
  val tdClass = "pageheader"
}

case class ItemIdData(firstByte: Int, lastByte: Int, content: String, itemHeader: ItemHeader) extends PageElement {
  val tdClass = "itemiddata"
}

case class ItemHeader(firstByte: Int, lastByte: Int, content: String, item: Item) extends PageElement {
  val tdClass = "itemheader"
}

case class Item(firstByte: Int, lastByte: Int, content: String, item: HeapPageItem) extends PageElement {
  val tdClass = "item"
}

case class Empty(firstByte: Int, lastByte: Int, content: String) extends PageElement {
  val tdClass = "empty"
}



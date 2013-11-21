package de.postgresqlinsideout.pagelayout.data

/**
 * A PostgreSQL page and its contents.
 * (cf. http://www.postgresql.org/docs/9.3/static/storage-page-layout.html)
 * Free space and special space are currently ignored.
 *
 * @author Steffen Hildebrandt
 */
class Page(val pageHeaderData: PageHeader, val itemIdData: List[ItemPos], val items: List[HeapPageItem]) {


}

object Page {

  def apply(pageHeaderData: PageHeader, items: List[HeapPageItem]) = {
    val itemIdData = items map (i => ItemPos(i.lpOff.value, i.lpLen.value))
    new Page(pageHeaderData, itemIdData, items)
  }


  val PAGE_HEADER_DATA_START = 0 // byte number on page
  val PAGE_HEADER_DATA_END = 23 // byte number on page

  val ITEM_ID_DATA_START = 24 // byte number on page

  val DATA_HEADER_SIZE = 23 // bytes
}

/**
 * The relative position and length of an item in a page
 * Just a wrapper for a Tuple2[Int, Int] to have the arguments named
 * @param off The offset (the current position) of the item in the page from
 * @param len The length of the item
 */
case class ItemPos(off: Int, len: Int) extends (Int, Int)(off, len)
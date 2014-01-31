/**
 * Copyright (c) 2013, Steffen Hildebrandt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.postgresqlinsideout.pagelayout.data

import de.postgresqlinsideout.pagelayout.visualization._
import de.postgresqlinsideout.pagelayout.visualization.ItemIdData
import de.postgresqlinsideout.pagelayout.visualization.PageHeader
import de.postgresqlinsideout.pagelayout.visualization.ItemHeader
import de.postgresqlinsideout.pagelayout.visualization.Item
import scala.slick.session.Database
import scala.collection.SortedSet
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * A PostgreSQL page and its contents.
 * (cf. http://www.postgresql.org/docs/9.3/static/storage-page-layout.html)
 * Special space is currently ignored.
 *
 * @author Steffen Hildebrandt
 */
class Page(val db: Database, val table: String, val pageNo: Int) {
  import Page._

  private lazy val pageHeaderData = DBAccess.getPageHeaderData(db, table, pageNo)
  protected lazy val heapPageItems = DBAccess.getHeapPageItemsData(db, table, pageNo)

  lazy val columnNames = DBAccess.getColumnNames(db, table)

  /**
   * Returns all elements in this page as a List of PageElement
   * The Elements are NOT sorted
   * @return a List of PageElements
   */
  lazy val pageElements = {
    val pageHeader = PageHeader(PAGE_HEADER_DATA_START, PAGE_HEADER_DATA_END, pageHeaderData)
    val pageSize = pageHeaderData.pagesize.value
    val otherElements = heapPageItems flatMap {hpi =>
      // we have to build "backwards" here to set the pointers
      val item = Item(hpi.itemDataStart, hpi.itemDataEnd, hpi)
      val itemHeader = ItemHeader(hpi.itemHeaderStart, hpi.itemHeaderEnd, item)
      val itemIdData = ItemIdData(hpi.itemIdDataStart, hpi.itemIdDataEnd, itemHeader)
      if (hpi.tCtid.value == None)
        List(itemIdData) // ctid == None means that item does not exist (state of itempointer should be 0)
      else
        List(itemIdData, itemHeader, item)
    }
    val content = pageHeader :: otherElements
    val empty = getEmptySpace(content, pageSize)
    empty ::: content
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

  def getEmptySpace(allNonEmptyElements: List[PageElement], tableSize: Int): List[PageElement] = {
    val sorted = List[PageElement](allNonEmptyElements:_*).sorted(PageElement.ordering)
    val it = (sorted map (e => (e.firstByte, e.lastByte))).iterator

    val empty = ListBuffer[Empty]()
    var last = 0
    while (it.nonEmpty) {
      val elem = it.next
      if (elem._1 - last > 1)
        empty += Empty(last + 1, elem._1 - 1)
      last = elem._2
    }
    if (last < tableSize)
      empty += Empty(last + 1, tableSize - 1)
    empty.toList
  }
}

object Page {

  val PAGE_HEADER_DATA_START = 0 // byte number on page
  val PAGE_HEADER_DATA_END = 23 // byte number on page

  val ITEM_ID_DATA_START = 24 // byte number on page

  val DATA_HEADER_FIXED_SIZE = 23 // in bytes, length of fixed part of tuple headers, does not include null bitmaps, padding, etc.

  def getVisualisationsOfAllPages(db: Database, table: String, layout: LayoutProperties): List[PageVisualization] = {
    def visOfPage(pageNo: Int): Try[PageVisualization] = {
      val p = new Page(db, table, pageNo)
      Try(new HtmlTable(p, layout))
    }
    val visStream = Stream from 0 map (n => visOfPage(n))
    val visualizations = (visStream takeWhile (_.isSuccess)).toList
    visualizations map {case Success(v) => v}
  }
}
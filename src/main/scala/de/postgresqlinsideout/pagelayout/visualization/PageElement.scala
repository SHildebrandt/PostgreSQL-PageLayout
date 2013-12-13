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

  val name: String = "id" + firstByte.toString
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
    case (Some(d), Some(t)) => s"${DBAccess.getContentForCtid(d, t, heapPageItem.tCtid.value) mkString " , "}"
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
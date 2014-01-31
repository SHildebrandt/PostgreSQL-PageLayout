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

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import scala.collection.immutable.IndexedSeq
import de.postgresqlinsideout.pagelayout.data.Page


/**
 * A class providing utilities to visualize the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable(page: Page, layout: LayoutProperties) extends PageVisualization(page) {
  import HtmlTable._
  import layout._

  def pageTitle = s"Visualization of Page $pageNo in Table $table"
  def pageSubtitle: Option[String] = query map ("under the condition '" + _ + "'")

  /** elements sorted and optionally ignored range excluded (if IGNORED_BYTE_RANGE != None) */
  lazy val contents: List[PageElement] = {
    val sorted = List[PageElement](elements:_*).sorted(PageElement.ordering)

    IGNORED_BYTE_RANGE match {
      case None => sorted
      case Some((start, end)) =>
        val firstElem = (sorted find (e => e.firstByte <= start && start <= e.lastByte)).get
        val lastElem = (sorted find (e => e.firstByte <= end && end <= e.lastByte)).get
        if (COMPRESS_INNER_ROWS && firstElem.lastByte >= lastElem.firstByte - 1) // firstElem and lastElem equal or next to each other
          sorted // don't need to cut out just one or two elements, since they will be compressed at any rate
        else
          Ignored(firstElem.firstByte, lastElem.lastByte) :: (sorted filter (e => e.lastByte < start || e.firstByte > end)) sorted (PageElement.ordering)
    }
  }

  private def startPos(item: PageElement) = (item.firstByte / COLUMNS + 1, item.firstByte % COLUMNS + 1)

  private def endPos(item: PageElement) = (item.lastByte / COLUMNS + 1, item.lastByte % COLUMNS + 1)

  /**
   * Prints the contents of this HtmlTable to the given file.
   * The result will be an html file with the contents placed into a table,
   * many of the properties can be adjusted in the field layout.
   *
   * The folder containing this file should contain a .css file defining the respective td classes,
   * this allows the user to easily change the look of the html file.
   * @param file The file which the html content should be printed to.
   */
  override def printToFile(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 1
    var currentColumn = 1

    def tr = writer.println("    <tr>")
    def `/tr` = writer.println("    </tr>")
    def td(name: String, clazz: String = "", colspan: Int = 1, title: String = "", mouseover: String = "") =
      writer.print(s"      <td class='content $clazz' name='$name' colspan=$colspan title='$title' $mouseover>" +
        "<div class='td' style=\"cursor: pointer;\" onclick=\"window.location='#" + name + "';\">")
    def `/td` = writer.println("</div></td>")

    def cell(colspan: Int, element: PageElement, useContinuedContent: Boolean) = {
      val mouseover = element match {
        case ItemIdData(_, _, h) if h.item.heapPageItem.lpFlags.value != 0 => emphasize(h.id) // don't emphasize if itempointer is dead
        case _ => ""
      }
      val title = s"Start Byte = ${element.firstByte}, Length = ${element.lastByte - element.firstByte + 1}\n${element.title}"
      td(element.id, element.tdClass, colspan, title, mouseover)
      if (!useContinuedContent)
        writer.print(element.content)
      else
        writer.print(element.contentContinued)
      `/td`
      currentColumn += colspan
    }

    def cellUntil(column: Int, element: PageElement, useContinuedContent: Boolean = false) = {
      if (column >= currentColumn) {
        if (currentColumn == 1) tr
        cell(column - currentColumn + 1, element: PageElement, useContinuedContent)
      }
      if (column == COLUMNS) {
        `/tr`
        currentColumn = 1
        currentRow += 1
      }
    }

    def rowsUntil(row: Int, element: PageElement) = {
      // (Range.)until excludes the last element!
      (currentRow until row) foreach (_ => cellUntil(COLUMNS, element, true))
    }

    def leftOutRows(element: PageElement) = {
      tr
      val title = s"Start Byte = ${element.firstByte}, Length = ${element.lastByte - element.firstByte + 1}\n${element.title}"
      writer.print(s"      <td name=${element.id} class='${element.tdClass} leftOutRows' colspan=$COLUMNS title='(rows compressed)\n$title'></td>")
      `/tr`
    }

    def content(endPos: Pos, element: PageElement) = {
      val (row, col) = endPos
      val contentRows = row - currentRow + 1
      if (contentRows == 1)
        cellUntil(col, element)
      else if (contentRows > 1) {
        val contentInNextRow =
          element.content.size > 0 &&
          currentColumn.toDouble / COLUMNS > 0.85 &&
          (contentRows > 2 || col.toDouble / COLUMNS > 0.15)                        
        val leaveOutRows =
          COMPRESS_INNER_ROWS &&
            (contentRows > 3 ||
              (contentRows == 3 && !contentInNextRow))

        cellUntil(COLUMNS, element, contentInNextRow) /* 'if contentInNextRow then useContinuedContent in first row' */

        if (contentInNextRow && contentRows > 2)
          cellUntil(COLUMNS, element, false)
        if (leaveOutRows || element.isInstanceOf[Ignored])
          leftOutRows(element)
        else
          rowsUntil(row, element)

        currentRow = row
        cellUntil(col, element, !(contentInNextRow && contentRows == 2))
      } else {
        // contentRows < 1  --> don't do anything (might happen when we have reached the end of the page)
      }
    }

    def contentDetails(element: PageElement) {
      writer.println(s"<div id='${element.id}' class='modal'>" + "<div style=\"cursor: pointer;\" onclick=\"window.location='';\">")
      writer.println(s"<p>Start Byte = ${element.firstByte}, Length = ${element.lastByte - element.firstByte + 1} bytes</p>")
      if (element.structuredContent.nonEmpty) {
        writer.println(modalTableHead)
        element.structuredContent.foreach(detail => {
          tr
          writer.println(s"      <td>${detail.name}</td>")
          writer.println(s"      <td>${detail.value}</td>")
          writer.println(s"      <td>${detail.description.getOrElse("")}</td>")
          `/tr`
        })
        writer.println(tableEnd)
      } else {
        element match {
          case _: Ignored => writer.println("<p>The creator of this visualization decided to ignore this page range to keep it compact.</p>")
          case _: Empty   => writer.println("<p>Well, I guess there's nothing more to say... It's just Empty Space!</p>")
          case i: Item if i.mightBeDead => writer.println("<p>This item seems to be dead, it will be removed by the next VACUUM operation.</p>")
          case _ => writer.println("<p>Sorry, no idea what to say about that...</p>")
        }
      }
      writer.println("</div></div>")
    }

    writer.println(htmlHead)
    writer.println(header)
    writer.println(bodyStart)
    contents.foreach(element => {
      contentDetails(element)
    })
    writer.println(tableHead(pageTitle, pageSubtitle, page.columnNames))
    contents.foreach(element => {
      content(endPos(element), element)
    })
    writer.println(tableEnd)
    writer.println(bodyEnd)
    writer.println(htmlEnd)
    writer.close()
  }
}

/**
 * Companion object
 *
 * @author Steffen Hildebrandt
 */
object HtmlTable {
  /**
   * Position (row, column) in a HtmlTable
   */
  type Pos = (Int, Int)
}
package de.postgresqlinsideout.pagelayout.visualization

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import scala.collection.immutable.IndexedSeq


/**
 * A class providing utilities to visualize the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable(elements: List[PageElement], table: String, pageNo: Int)
  extends PageVisualization with LayoutProperties {

  import HtmlTable._

  override def pageTitle = s"Visualization of Page $pageNo in Table $table"

  /** elements sorted and optionally ingnored range excluded (if IGNORED_BYTE_RANGE != None)*/
  val contents: SortedSet[PageElement] = { 
    val sorted = SortedSet[PageElement](elements:_*)(PageElement.ordering)

    IGNORED_BYTE_RANGE match {
      case None => sorted
      case Some((start, end)) =>
        val firstElem = (sorted find (e => e.firstByte <= start && start <= e.lastByte)).get
        val lastElem = (sorted find (e => e.firstByte <= end && end <= e.lastByte)).get
        if (COMPRESS_INNER_ROWS && firstElem.lastByte >= lastElem.firstByte - 1) // firstElem and lastElem equal or next to each other
          sorted // don't need to cut out just one or two elements, since they will be compressed at any rate
        else
          (sorted filter (e => e.lastByte < start || e.firstByte > end)) + Ignored(firstElem.firstByte, lastElem.lastByte)
    }
  }

  private def startPos(item: PageElement) = (item.firstByte / COLUMNS + 1, item.firstByte % COLUMNS + 1)

  private def endPos(item: PageElement) = (item.lastByte / COLUMNS + 1, item.lastByte % COLUMNS + 1)

  override def printToFile(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 1
    var currentColumn = 1

    def tr = writer.println("    <tr>")
    def `/tr` = writer.println("    </tr>")
    def td(name: String, clazz: String = "", colspan: Int = 1, title: String = "", mouseover: String = "") =
      writer.print(s"      <td name='$name' class='$clazz' colspan=$colspan title='$title' $mouseover><div class='td'>")
    def `/td` = writer.println("</div></td>")

    def cell(colspan: Int, element: PageElement, useContinuedContent: Boolean) = {
      val mouseover = element match {
        case ItemIdData(_, _, h) => emphasize(h.name)
        case _ => ""
      }
      val title = s"Start Byte = ${element.firstByte}, Length = ${element.lastByte - element.firstByte + 1}\n${element.title}"
      td(element.name, element.tdClass, colspan, title, mouseover)
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
      writer.print(s"      <td name='' class='${element.tdClass} leftOutRows' colspan=$COLUMNS title='$title'></td>")
      `/tr`
    }

    def content(endPos: Pos, element: PageElement) = {
      val row = endPos._1
      val col = endPos._2
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
        if (leaveOutRows)
          leftOutRows(element)
        else
          rowsUntil(row, element)

        currentRow = row
        cellUntil(col, element, !(contentInNextRow && contentRows == 2))
      } else {
        // contentRows < 1  --> don't do anything (might happen when we have reached the end of the page)
      }
    }

    writer.println(htmlHead)
    writer.println(header)
    writer.println(tableHead)
    contents.foreach(element => {
      content(endPos(element), element)
    })
    writer.println(tableEnd)
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
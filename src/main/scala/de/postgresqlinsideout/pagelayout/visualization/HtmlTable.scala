package de.postgresqlinsideout.pagelayout.visualization

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import scala.collection.immutable.IndexedSeq


/**
 * A class providing utilities to visualize the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable(elements: List[PageElement]) extends PageVisualization with LayoutProperties {

  import HtmlTable._

  val tableItemOrdering: Ordering[PageElement] = Ordering[Int].on[PageElement](t => t.firstByte)
  /** elements + Empty elements */
  val contents: SortedSet[PageElement] = { 
    val sorted = SortedSet[PageElement](elements:_*)(tableItemOrdering)
    val nonEmpty = sorted flatMap (e => (e.firstByte to e.lastByte))
    val empty = (1 to TABLE_SIZE) filterNot (nonEmpty contains _)
    sorted ++ (empty map (i => Empty(i, i)))
  }

  //override def addItem(item: PageElement) = contents += item

  /*
  override def getHovers = {
    contents flatMap { // filter ItemIdData
      case i : ItemIdData => Some(i)
      case _              => None
    } map {c =>
      s"table td#${c.id}:hover ~ #${c.itemHeader.id} { $EMPHASIZE_STYLE }"
    } mkString "\n"
  }
  */

  private def startPos(item: PageElement) = (item.firstByte / COLUMNS + 1, item.firstByte % COLUMNS + 1)

  private def endPos(item: PageElement) = (item.lastByte / COLUMNS + 1, item.lastByte % COLUMNS + 1)

  override def printToFile(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 1
    var currentColumn = 1

    def tr = writer.println("    <tr>")
    def `/tr` = writer.println("    </tr>")
    def td(id: String = "", clazz: String = "", colspan: Int = 1, mouseover: String = "") =
      writer.print(s"      <td id=$id class='$clazz' colspan=$colspan $mouseover>")
    def `/td` = writer.println("</td>")

    def cell(colspan: Int, element: PageElement) = {
      val mouseover = element match {
        case ItemIdData(_, _, h) => emphasize(h.id)
        case _ => ""
      }
      td(element.id, element.tdClass, colspan, mouseover)
      if (element.content != "")
        writer.print(element.content)
      `/td`
      currentColumn += colspan
    }

    def cellUntil(column: Int, element: PageElement) = {
      if (column >= currentColumn) {
        if (currentColumn == 1) tr
        cell(column - currentColumn + 1, element: PageElement)
      }
      if (column == COLUMNS) {
        `/tr`
        currentColumn = 1
        currentRow += 1
      }
    }

    def rowsUntil(row: Int, element: PageElement) = {
      // (Range.)until excludes the last element!
      (currentRow until row) foreach (_ => cellUntil(COLUMNS, element))
    }

    def content(endPos: Pos, element: PageElement) = {
      val row = endPos._1
      val col = endPos._2
      if (row == currentRow)
        cellUntil(col, element)
      else if (row > currentRow) {
        // might want to optimize content position...
        // TODO: Fix continuedString
        val continuedString = if (element.content == "") "" else "..."
        cellUntil(COLUMNS, element)
        rowsUntil(row, element)
        cellUntil(col, element)
      } else {
        // row < currentRow  --> don't do anything (might have reached the end of the page)
      }
    }

    writer.println(htmlHead)
    writer.println(header)
    writer.println(tableHead)
    contents.foreach(element => {
      content(endPos(element), element)
    })
    // if (currentColumn != 1) `/tr`
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
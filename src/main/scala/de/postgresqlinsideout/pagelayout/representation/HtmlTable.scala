package de.postgresqlinsideout.pagelayout.representation

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import de.postgresqlinsideout.pagelayout.representation.ContentType._


/**
 * A class providing utilities to illustrate the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable extends PageRepresentation with LayoutProperties {

  import HtmlTable._

  val tableItemOrdering: Ordering[PageElement] = Ordering[Int].on[PageElement](t => t.firstByte)
  private var contents = SortedSet[PageElement]()(tableItemOrdering)

  override def addItem(item: PageElement) = contents += item

  override def getHovers = {
    contents flatMap { // filter ItemIdData
      case i : ItemIdData => Some(i)
      case _              => None
    } map {c =>
      s"#${c.id}:hover ~ #${c.itemHeader.id} { $HOVER_STYLE }"
    } mkString "\n"
  }

  private def startPos(item: PageElement) = (item.firstByte / COLUMNS + 1, item.firstByte % COLUMNS + 1)

  private def endPos(item: PageElement) = (item.lastByte / COLUMNS + 1, item.lastByte % COLUMNS + 1)

  override def printToFile(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 1
    var currentColumn = 1

    def tr = writer.println("    <tr>")
    def `/tr` = writer.println("    </tr>")
    def td(clazz: String = "", colspan: Int = 1) = writer.print(s"      <td colspan=$colspan class='$clazz'>")
    def `/td` = writer.println("</td>")

    def cell(colspan: Int, id: String, contentType: ContentType, content: String = "") = {
      td(contentType.tdClass, colspan)
      if (content != "")
        writer.print(content)
      `/td`
      currentColumn += colspan
    }

    def cellUntil(column: Int, id: String, contentType: ContentType, content: String = "") = {
      if (column >= currentColumn) {
        if (currentColumn == 1) tr
        cell(column - currentColumn + 1, id, contentType, content)
      }
      if (column == COLUMNS) {
        `/tr`
        currentColumn = 1
        currentRow += 1
      }
    }

    def rowsUntil(row: Int, id: String, contentType: ContentType, content: String = "") = {
      // (Range.)until excludes the last element!
      (currentRow until row) foreach (_ => cellUntil(COLUMNS, id, contentType, content))
    }

    def content(endPos: Pos, id: String, contentType: ContentType, content: String = "") = {
      val row = endPos._1
      val col = endPos._2
      if (row == currentRow)
        cellUntil(col, id, contentType, content)
      else if (row > currentRow) {
        // might want to optimize content position...
        val continuedString = if (content == "") "" else "..."
        cellUntil(COLUMNS, id, contentType, content)
        rowsUntil(row, id, contentType, continuedString)
        cellUntil(col, id, contentType, continuedString)
      } else {
        // row < currentRow  --> don't do anything (might have reached the end of the page)
      }
    }

    def emptySpace(nextItem: Pos) = content((nextItem._1, nextItem._2 - 1), "", EMPTY, "")

    writer.println(htmlHead)
    writer.println(header)
    writer.println(tableHead)
    contents.foreach(item => {
      emptySpace(startPos(item))
      content(endPos(item), item.htmlId, item.contentType, item.content)
    })
    emptySpace((ROWS, COLUMNS + 1)) // fill remaining empty space
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
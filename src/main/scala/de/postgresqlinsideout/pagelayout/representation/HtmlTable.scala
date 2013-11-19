package de.postgresqlinsideout.pagelayout.representation

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import de.postgresqlinsideout.pagelayout.representation.ContentType._

/**
 * Trait for a page representation.
 * So far the only implementation is a HtmlTable.
 *
 * @author Steffen Hildebrandt
 */
trait PageRepresentation {
  def addItem(item: PageItem)
  def printToFile(to: File)
}

/**
 * A class providing utilities to illustrate the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable extends PageRepresentation with LayoutProperties {
  import HtmlTable._

  val tableItemOrdering: Ordering[PageItem] = Ordering[Int].on[PageItem](t => t.firstByte)
  private var contents = SortedSet[PageItem]()(tableItemOrdering)

  override def addItem(item: PageItem) = contents += item

  private def startPos(item: PageItem) = (item.firstByte / COLUMNS + 1, item.firstByte % COLUMNS + 1)

  private def endPos(item: PageItem) = (item.lastByte / COLUMNS + 1, item.lastByte % COLUMNS + 1)

  override def printToFile(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 1
    var currentColumn = 1

    def tr = writer.println("    <tr>")
    def `/tr` = writer.println("    </tr>")
    def td(clazz: String = "default", colspan: Int = 1) = writer.print(s"      <td colspan=$colspan class='$clazz'>")
    def `/td` = writer.println("</td>")

    def cell(colspan: Int, contentType: ContentType, content: String = "") = {
      td(contentType.tdClass, colspan)
      if (content != "")
        writer.print(content)
      `/td`
      currentColumn += colspan
    }

    def cellUntil(column: Int, contentType: ContentType, content: String = "") = {
      if (column >= currentColumn) {
        if (currentColumn == 1) tr
        cell(column - currentColumn + 1, contentType, content)
      }
      if (column == COLUMNS) {
        `/tr`
        currentColumn = 1
        currentRow += 1
      }
    }

    def rowsUntil(row: Int, contentType: ContentType, content: String = "") = {
      // (Range.)until excludes the last element!
      (currentRow until row) foreach (_ => cellUntil(COLUMNS, contentType, content))
    }

    def content(endPos: Pos, contentType: ContentType, content: String = "") = {
      val row = endPos._1
      val col = endPos._2
      if (row == currentRow)
        cellUntil(col, contentType, content)
      else if (row > currentRow) {
        // might want to optimize content position...
        val continuedString = if (content == "") "" else "..."
        cellUntil(COLUMNS, contentType, content)
        rowsUntil(row, contentType, continuedString)
        cellUntil(col, contentType, continuedString)
      } else {
        // row < currentRow  --> don't do anything (might have reached the end of the page)
      }
    }

    def emptySpace(nextItem: Pos) = content((nextItem._1, nextItem._2 - 1), EMPTY, "")

    writer.println(htmlHead)
    writer.println(header)
    writer.println(tableHead)
    contents.foreach(item => {
      emptySpace(startPos(item))
      content(endPos(item), item.contentType, item.content)
    })
    emptySpace((ROWS, COLUMNS+1)) // fill remaining empty space
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
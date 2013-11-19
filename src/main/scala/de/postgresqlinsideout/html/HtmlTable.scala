package de.postgresqlinsideout.html

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import ContentType._

/**
 * A class providing utilities to illustrate the content of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
class HtmlTable extends LayoutProperties {
  import HtmlTable._

  val tableItemOrdering = Ordering[Int].on[TableItem](t => t.firstByte)
  private var contents = SortedSet[TableItem]()(tableItemOrdering)

  def addItem(item: TableItem) = contents += item

  private def startPos(item: TableItem) = (item.firstByte / COLUMNS, item.firstByte % COLUMNS)

  private def endPos(item: TableItem) = (item.lastByte / COLUMNS, item.lastByte % COLUMNS)

  def printHTML5(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 0
    var currentColumn = 0

    def tr(rowspan: Int = 1) = writer.println(s"    <tr rowspan=$rowspan>")
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
        if (currentColumn == 0) tr()
        cell(column - currentColumn + 1, contentType, content)
      }
      if (column == COLUMNS) {
        `/tr`
        currentColumn = 0
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
      else {
        // might want to optimize content position...
        val continuedString = if (content == "") "" else "..."
        cellUntil(COLUMNS, contentType, content)
        rowsUntil(row, contentType, continuedString)
        cellUntil(col, contentType, continuedString)
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
    if (currentColumn != 0) `/tr`
    writer.println(tableEnd)
    writer.println(htmlEnd)
    writer.close()
  }

}

object HtmlTable {

  /**
   * Position (column, row) in a HtmlTable
   */
  type Pos = (Int, Int)
}

case class TableItem(firstByte: Int, lastByte: Int, contentType: ContentType, content: String)

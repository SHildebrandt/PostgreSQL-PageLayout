package de.postgresqlinsideout

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import de.postgresqlinsideout.HtmlTable._

/**
 * @author Steffen Hildebrandt
 */
class HtmlTable {
  import ContentType._

  val tableItemOrdering = Ordering[Int].on[TableItem](t => t.firstByte)
  private var contents = SortedSet[TableItem]()(tableItemOrdering)

  def addTableItem(item: TableItem) = contents += item

  def printHTML5(file: File) = {
    val writer = new PrintWriter(file)
    var currentRow = 0
    var currentColumn = 0

    def tr(rowspan: Int = 1) = writer.println(s"  <tr rowspan=$rowspan>")
    def `/tr` = writer.println("  </tr>")
    def td(clazz: String = "default", colspan: Int = 1) = writer.print(s"    <td colspan=$colspan class=$clazz>")
    def `/td` = writer.println("    </td>")

    def cell(colspan: Int, contentType: ContentType, content: String = "") = {
      td(contentType.tdClass, colspan)
      if (content != "")
        writer.print(content)
      `/td`
      currentColumn += colspan
    }

    def cellUntil(column: Int, contentType: ContentType, content: String = "") = {
      if (column > currentColumn) {
        if (currentColumn == 0) tr()
        cell(COLUMNS - currentColumn, contentType, content)
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

    def emptySpace(nextItem: Pos) = content(nextItem, EMPTY, "")

    writer.println("<table>")
    contents.foreach(item => {
      emptySpace(item.startPos)
      content(item.endPos, item.contentType, item.content)
    })
    writer.println("</table>")
    writer.close()
  }

}

object HtmlTable {

  type Pos = (Int, Int)

  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  val ROWS = 128 // 8192 / 64

}

object ContentType extends Enumeration {
  case class ContentTypeVal(name: String, tdClass: String) extends Val(name)

  type ContentType = ContentTypeVal
  val DATA = ContentTypeVal("Data", "Data")
  val HEADER = ContentTypeVal("Header", "Header")
  val EMPTY = ContentTypeVal("Empty", "Empty")
}

import ContentType._
case class TableItem(firstByte: Int, lastByte: Int, contentType: ContentType, content: String) {
  val startPos = (firstByte / COLUMNS, firstByte % COLUMNS)
  val endPos = (lastByte / COLUMNS, lastByte % COLUMNS)
}

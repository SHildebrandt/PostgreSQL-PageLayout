package de.postgresqlinsideout

import scala.collection.SortedSet
import java.io.{PrintWriter, File}
import de.postgresqlinsideout.HtmlTable._
import ContentType._

/**
  * @author Steffen Hildebrandt
 */
class HtmlTable {

  val tableItemOrdering = Ordering[Int].on[TableItem](t => t.firstByte)
  private var contents = SortedSet[TableItem]()(tableItemOrdering)

  def addItem(item: TableItem) = contents += item

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
      emptySpace(item.startPos)
      content(item.endPos, item.contentType, item.content)
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

  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  val ROWS = TABLE_SIZE / COLUMNS

  val TABLE_WIDTH = 1000 // px
  val ROW_HEIGHT = 20 // px
  val COLUMN_WIDTH = TABLE_WIDTH / COLUMNS

  val htmlHead = """<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
                   |<html>""".stripMargin

  val header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |<link rel="stylesheet" type="text/css" href="style.css">
                  |</head>""".stripMargin

  val tableHead = {
    val body = "<body><div align='center'>\n"
    val table = "  <table>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col width='$COLUMN_WIDTH'/>\n") mkString ""
    body + table + cols
  }

  val tableEnd = "  </table></div>\n</body>"

  val htmlEnd = "</html>"

}

case class TableItem(firstByte: Int, lastByte: Int, contentType: ContentType, content: String) {
  val startPos = (firstByte / COLUMNS, firstByte % COLUMNS)
  val endPos = (lastByte / COLUMNS, lastByte % COLUMNS)
}

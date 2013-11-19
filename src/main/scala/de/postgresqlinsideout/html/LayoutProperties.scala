package de.postgresqlinsideout.html

/**
 * A trait providing basic constants for the layout of a @link{HtmlTable}
 *
 * @author Steffen Hildebrandt
 */
trait LayoutProperties {

  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  val ROWS = TABLE_SIZE / COLUMNS

  val TABLE_WIDTH = 1000 // px
  val ROW_HEIGHT = 20 // px
  val COLUMN_WIDTH = TABLE_WIDTH / COLUMNS

  val htmlHead = """<!DOCTYPE html>
                   |<html>""".stripMargin

  val header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |<link rel="stylesheet" type="text/css" href="style.css">
                  |</head>""".stripMargin

  val tableHead = {
    val body = "<body><div align='center'>\n"
    val table = "  <table>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col width='$COLUMN_WIDTH'/>") mkString "\n"
    body + table + cols
  }

  val tableEnd = "  </table></div>\n</body>"

  val htmlEnd = "</html>"
}

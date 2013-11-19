package de.postgresqlinsideout.pagelayout.representation

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

  val style = s"""<link rel="stylesheet" type="text/css" href="style.css">
                 |<style type="text/css">
                 |  col.fixedWidth { width:${COLUMN_WIDTH}px; }
                 |</style>""".stripMargin

  val header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |$style
                  |</head>""".stripMargin

  val tableHead = {
    val body = "<body>\n"
    val table = "  <table class='center'>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col class='fixedWidth'/>") mkString "\n"
    body + table + cols
  }

  val tableEnd = "  </table>\n</body>"

  val htmlEnd = "</html>"
}

package de.postgresqlinsideout.pagelayout.visualization

/**
 * A trait providing basic constants for the layout of a @link{HtmlTable}
 *
 * @author Steffen Hildebrandt
 */
trait LayoutProperties {

  /**
   * Returns a String containing CSS style definitions with hovers
   * (-> mouseovers, e.g. to highlight the entry which corresponds to a field in ItemIdData)
   * @return a String with hover definitions
   */
  def getHovers: String
  
  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  val ROWS = TABLE_SIZE / COLUMNS

  val TABLE_WIDTH = 1000 // px
  val ROW_HEIGHT = 20 // px
  val COLUMN_WIDTH = TABLE_WIDTH / COLUMNS

  val HOVER_STYLE = "background-color:red;"

  def htmlHead = """<!DOCTYPE html>
                   |<html>""".stripMargin

  def style = s"""<link rel="stylesheet" type="text/css" href="style.css">
                 |<style type="text/css">
                 |  col.fixedWidth { width:${COLUMN_WIDTH}px; }
                 |$getHovers
                 |</style>""".stripMargin

  def header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |$style
                  |</head>""".stripMargin

  def tableHead = {
    val body = "<body>\n"
    val table = "  <table class='center'>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col class='fixedWidth'/>") mkString "\n"
    body + table + cols
  }

  def tableEnd = "  </table>\n</body>"

  def htmlEnd = "</html>"
}

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
  //def getHovers: String
  
  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  val ROWS = TABLE_SIZE / COLUMNS

  val TABLE_WIDTH = 1000 // px
  val ROW_HEIGHT = 20 // px
  val COLUMN_WIDTH = TABLE_WIDTH / COLUMNS

  val EMPHASIZE_FUNCTION_NAME = "emphasize"
  val UNEMPHASIZE_FUNCTION_NAME = "unemphasize"
  val EMPHASIZE_STYLE = "backgroundColor = \"blue\""

  /**
    * Should the inner rows be compressed, if a PageElement reaches over more than 2 rows?
   */
  val COMPRESS_INNER_ROWS = true

  /**
   * This field can be used to cut out/ignore a byte range of the page
   * (might make sense for tables with many columns,
   * where COMPRESS_INNER_ROWS doesn't keep the visualization small enough)
   */
  val IGNORED_BYTE_RANGE: Option[(Int, Int)] = Some(1000, 6000)

  def htmlHead = """<!DOCTYPE html>
                   |<html>""".stripMargin

  def style = s"""<link rel="stylesheet" type="text/css" href="style.css">
                 |<style type="text/css">
                 |  col.fixedWidth { width:${COLUMN_WIDTH}px; }
                 |</style>""".stripMargin

  def header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |$style
                  |<script language="javascript" type="text/javascript">
                  |function $EMPHASIZE_FUNCTION_NAME(id) {
                  |   document.getElementById(id).style.$EMPHASIZE_STYLE;
                  |}
                  |function $UNEMPHASIZE_FUNCTION_NAME(id) {
                  |   document.getElementById(id).style.backgroundColor = \"\";
                  |}
                  |</script>
                  |</head>""".stripMargin

  def tableHead = {
    val body = "<body>\n"
    val table = "  <table class='center'>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col class='fixedWidth'/>") mkString "\n"
    body + table + cols
  }

  def tableEnd = "  </table>\n</body>"

  def htmlEnd = "</html>"

  def emphasize(id: String) = // escaping quotes in String interpolations doesn't work.....
    s"onmouseover=${'"'}$EMPHASIZE_FUNCTION_NAME('$id')${'"'} onmouseout=${'"'}$UNEMPHASIZE_FUNCTION_NAME('$id')${'"'}"
}

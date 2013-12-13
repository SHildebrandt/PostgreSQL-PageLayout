package de.postgresqlinsideout.pagelayout.visualization

/**
 * A trait providing basic constants for the layout of a @link{HtmlTable}
 *
 * @author Steffen Hildebrandt
 */
trait LayoutProperties {

  val TABLE_SIZE = 8192 // bytes
  val COLUMNS = 64
  lazy val ROWS = {
    assert(TABLE_SIZE % COLUMNS == 0, "Please choose a COLUMNS value that makes sense! It should be a divisor of the TABLE_SIZE.")
    TABLE_SIZE / COLUMNS
  }

  val TABLE_WIDTH = 16 * 64 // px
  val ROW_HEIGHT = 10 // px
  lazy val COLUMN_WIDTH = {
    assert(TABLE_WIDTH % COLUMNS == 0,
      s"Please choose a TABLE_WIDTH value that fits! (is a multiple of the number of COLUMNS ($COLUMNS)")
    TABLE_WIDTH / COLUMNS
  }

  val EMPHASIZE_FUNCTION_NAME = "emphasize"
  val UNEMPHASIZE_FUNCTION_NAME = "unemphasize"

  /**
    * Should the inner rows be compressed, if a PageElement reaches over more than 2 rows?
   */
  val COMPRESS_INNER_ROWS = true

  /**
   * This field can be used to cut out/ignore a byte range of the page
   * (might make sense for tables with many columns,
   * where COMPRESS_INNER_ROWS doesn't keep the visualization small enough)
   */
  val IGNORED_BYTE_RANGE: Option[(Int, Int)] = Some(1500, 7000)

  def htmlHead = """<!DOCTYPE html>
                   |<html>""".stripMargin

  def style = s"""<link rel="stylesheet" type="text/css" href="style.css">
                 |<style type="text/css">
                 |  col.fixedWidth { width:${COLUMN_WIDTH}px; }
                 |  td { height:${ROW_HEIGHT}px; }
                 |</style>""".stripMargin

  def header = s"""<head>
                  |<title>PostgreSQL PageLayout</title>
                  |$style
                  |<script language="javascript" type="text/javascript">
                  |function $EMPHASIZE_FUNCTION_NAME(name) {
                  |   var elems = document.getElementsByName(name);
                  |   for (var i = 0; i < elems.length; i++)
                  |       elems[i].classList.add("emphasize");
                  |}
                  |function $UNEMPHASIZE_FUNCTION_NAME(name) {
                  |   var elems = document.getElementsByName(name);
                  |   for (var i = 0; i < elems.length; i++)
                  |       elems[i].classList.remove("emphasize");
                  |}
                  |</script>
                  |</head>""".stripMargin

  def tableHead(pageTitle: String, pageSubTitle: Option[String], columnNames: List[String]) = {
    val body = "<body>\n"
    val title = s"  <h1>$pageTitle</h1>\n"
    val subtitle = if (!pageSubTitle.isDefined) "" else s"  <h2>${pageSubTitle.get}</h2>\n"
    val columnDescription = s"  <h3>Column Names: ${columnNames.mkString(", ")}</h3>\n"
    val table = "  <table class='center'>\n"
    val cols = (1 to COLUMNS) map (_ => s"    <col class='fixedWidth'/>") mkString "\n"
    body + title + subtitle + columnDescription + table + cols
  }

  def tableEnd = "  </table>\n</body>"

  def htmlEnd = "</html>"

  def emphasize(name: String) = // escaping quotes in String interpolations doesn't work.....
    s"onmouseover=${'"'}$EMPHASIZE_FUNCTION_NAME('$name')${'"'} onmouseout=${'"'}$UNEMPHASIZE_FUNCTION_NAME('$name')${'"'}"
}

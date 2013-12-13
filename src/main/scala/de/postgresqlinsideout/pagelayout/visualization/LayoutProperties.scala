/**
 * Copyright (c) 2013, Steffen Hildebrandt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

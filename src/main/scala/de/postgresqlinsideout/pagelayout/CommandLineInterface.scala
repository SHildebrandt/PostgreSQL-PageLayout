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

package de.postgresqlinsideout.pagelayout

import scopt.OptionParser
import scala.slick.session.Database
import de.postgresqlinsideout.pagelayout.visualization.{HtmlTable, LayoutProperties}
import de.postgresqlinsideout.pagelayout.data.{Query, Page}
import java.io.File

object CommandLineInterface extends App {

  lazy val PAGE_FILE_EXTENSION = "_page_"

  override def main(args: Array[String]) {
    val config = parser parse (args, Config())
    config match {
      // parsing failed
      case None => ()
      // parsing successful
      case Some(c) =>
        c.pageNo match {
          // print page number n
          case Right(n) =>
            val page = c.condition match {
              // visualize whole page
              case None => new Page(c.database, c.table, n)
              // visualize query
              case Some(cond) => new Query(c.database, c.table, cond, n)
            }
            val visualization = new HtmlTable(page, c.layoutProperties)
            visualization.printToFile(new File(c.outputFile))
          // print all pages
          case Left(_) =>
            val visualizations = c.condition match {
              // visualize whole page
              case None => Page.getVisualisationsOfAllPages(c.database, c.table, c.layoutProperties)
              // visualize query
              case Some(cond) => Query.getVisualisationsOfAllPages(c.database, c.table, cond, c.layoutProperties)
            }
            visualizations.zipWithIndex foreach {case (v, i) =>
              val f = c.outputFile
              val fileName = if (f.endsWith(".html")) f.substring(0, f.size - 5) else f
              v.printToFile(new File(fileName + PAGE_FILE_EXTENSION + i + ".html"))
            }
        }
    }
  }

  case class Config(db: String = null, host: String = "localhost", user: String = "postgres", password: String = "postgres",
                    table: String = null, pageNo: Either[Unit, Int] = Right(-1), condition: Option[String] = None, outputFile: String = null,
                    compressInnerRows: Boolean = true, ignoredByteRange: Option[(Int, Int)] = None,
                    tableSize: Option[Int] = None, columns: Option[Int] = None,
                    tableWidth: Option[Int] = None, rowHeight: Option[Int] = None) {
    lazy val database = Database.forURL(s"jdbc:postgresql://$host/$db", user = user, password = password)

    val standardProps = new LayoutProperties {}

    lazy val layoutProperties = new LayoutProperties {
      override val COMPRESS_INNER_ROWS = compressInnerRows

      override val IGNORED_BYTE_RANGE = ignoredByteRange

      override val TABLE_SIZE = tableSize match {
        case None => standardProps.TABLE_SIZE
        case Some(t) => t
      }

      override val COLUMNS = columns match {
        case None => standardProps.COLUMNS
        case Some(c) => c
      }

      override val TABLE_WIDTH = tableWidth match {
        case None => standardProps.TABLE_WIDTH
        case Some(w) => w
      }

      override val ROW_HEIGHT = rowHeight match {
        case None => standardProps.ROW_HEIGHT
        case Some(h) => h
      }
    }
  }

  lazy val parser = new OptionParser[Config]("PageLayout") {
    head("PostgreSQL PageLayout\n")

    note(
      """Example usage: "-d booktown -t authors -n 0 -f output/testConfig.html"
        |
        |You can also generate a page for a specific query. In this case you should also give the condition (WHERE ...)
        |
        |Example: "-d booktown -t authors -n 0 -f output/testConfig.html -q "WHERE author_id > 2000"
        |
        |Required options:
      """.stripMargin)

    opt[String]('d', "database") action {
      (d, config) =>
        config.copy(db = d)
    } text ("The database to use") required()

    opt[String]('t', "table") action {
      (t, config) =>
        config.copy(table = t)
    } text ("The table which should be visualized") required()

    opt[Int]('n', "page-no") action {
      (n, config) =>
        config.copy(pageNo = Right(n))
    } text ("The number of the page to be visualized") validate {
      n =>
        if (n >= 0) success else failure("Page number must be greater than 0")
    }

    opt[Unit]('a', "all-pages") action {
      (_, config) =>
        config.copy(pageNo = Left())
    } text {"Create visualizations of all pages instead of a single one"}

    opt[String]('f', "file") action {
      (f, config) =>
        config.copy(outputFile = f)
    } text ("The output file to which the visualization is printed") required() valueName ("<file>")

    note("\nOptional arguments:\n")

    opt[String]('q', "condition") action {
      (q, config) =>
        config.copy(condition = Some(q))
    } text ("Visualizes only the entries for which the condition holds. This should be something like \"WHERE author_id = 42\".")

    opt[String]('h', "host") action {
      (h, config) =>
        config.copy(host = h)
    } text ("The database host. Default is localhost")

    opt[String]('u', "user") action {
      (u, config) =>
        config.copy(user = u)
    } text ("The user of the database. Default is postgres")

    opt[String]('p', "password") action {
      (p, config) =>
        config.copy(password = p)
    } text ("The password of the user. Default is postgres")

    opt[Boolean]('c', "compress") action {
      (c, config) =>
        config.copy(compressInnerRows = c)
    } text ("True, if inner rows should be compressed. This is useful to reduce the table size, " +
      "if some content reaches over several rows. By default set to true.")

    // (Int, Int) would be easier, but this is for key-value-pairs and I cannot change the implicit Read val
    opt[String]('i', "ignore-range") action {
      (s, config) =>
        val Re = """\((\d+),(\d+)\)""".r
        try {
          val Re(a, b) = s
          config.copy(ignoredByteRange = Some(a.toInt, b.toInt))
        } catch {
          case e: Exception => {
            println("Parsing the ignore-range tuple failed. Maybe you accidentally used a space within the tuple?")
            throw e
          }
        }
    } text ("This allows you to ignore a whole range of bytes in the visualization. " +
      "The ignored range will be shown as one (compressed) entry. " +
      "This will also help to reduce the table size.") valueName ("(<from>,<to>)")

    opt[Int]("page-size") action {
      (s, config) =>
        config.copy(tableSize = Some(s))
    } text ("The page size of a database page")

    opt[Int]("columns") action {
      (c, config) =>
        config.copy(columns = Some(c))
    } text ("The number of columns in the HTML-Page. Should be a divisor of the table size.") validate {
      c =>
        if (c >= 0) success else failure("Number of columns should be greater than 0")
    }

    opt[Int]("table-width") action {
      (w, config) =>
        config.copy(tableWidth = Some(w))
    } text ("The width of the HTML-Table in px. " +
      "Note: For small values, the browser might widen the table (columns) automatically.")

    opt[Int]("row-height") action {
      (h, config) =>
        config.copy(rowHeight = Some(h))
    } text ("The height of a row in the HTML-Table in px." +
      "Note: For small values, the browser might increase the height automatically.")

    help("help") text ("Prints this help text")

    checkConfig {
      _.pageNo match {
        case Left(_) => success
        case Right(i) => if (i >= 0) success else failure("One of the options page-no or all-pages is required!")
      }
    }
  }

}

package de.postgresqlinsideout.pagelayout

import scopt.{Read, OptionParser}
import scala.slick.session.Database
import de.postgresqlinsideout.pagelayout.visualization.LayoutProperties
import de.postgresqlinsideout.pagelayout.data.{Query, Page}
import java.io.File


object CommandLineInterface extends App {

  override def main(args: Array[String]) {
    parser.parse(args, Config()) map {
      c =>
        val page = new Page(c.database, c.table, c.pageNo)
        val visualization = page.getPageVisualization(c.layoutProperties)
        visualization.printToFile(c.outputFile)
    }
  }

  case class Config(db: String = null, host: String = "localhost", user: String = "postgres", password: String = "postgres",
                    table: String = null, pageNo: Int = -1, condition: Option[String] = None, outputFile: File = null,
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
        config.copy(pageNo = n)
    } text ("The number of the page to be visualized") required() validate {
      n =>
        if (n >= 0) success else failure("Page number must be greater than 0")
    }

    opt[String]('f', "file") action {
      (f, config) =>
        config.copy(outputFile = new File(f))
    } text ("The output file to which the visualization is printed") required() valueName ("<file>") validate {
      f =>
        if (true) success else failure(s"The given file is not valid: $f")
    }

    note("\nOptional arguments:\n")

    opt[String]('h', "host") action {
      (h, config) =>
        config.copy(host = h)
    } text ("The database host (e.g. localhost)")

    opt[String]('u', "user") action {
      (u, config) =>
        config.copy(user = u)
    } text ("The user of the database")

    opt[String]('p', "password") action {
      (p, config) =>
        config.copy(password = p)
    } text ("The password of the user")

    opt[Boolean]('c', "compress") action {
      (c, config) =>
        config.copy(compressInnerRows = c)
    } text ("True, if inner rows should be compressed. This is useful to reduce the table size, " +
      "if some content reaches over several rows.")

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

    opt[Int]('w', "table-width") action {
      (w, config) =>
        config.copy(tableWidth = Some(w))
    } text ("The width of the HTML-Table in px")

    opt[Int]("row-height") action {
      (h, config) =>
        config.copy(rowHeight = Some(h))
    } text ("The height of a row in the HTML-Table in px")

    help("help") text ("Prints this help text")
  }


  def test(args: Array[String]) {

    //val db = Database.forURL("jdbc:postgresql://localhost/dell", user = "postgres", password = "postgres")
    val db = Database.forURL("jdbc:postgresql://localhost/booktown", user = "postgres", password = "postgres")

    val page = new Page(db, "books", 0)  // other tables for dell: orders, orderlines, cust_hist, products

    page.getPageVisualization().printToFile(new File("output/PageLayout.html"))

    val query = new Query(db, "books", "WHERE author_id > 5000", 0)

    query.getPageVisualization().printToFile(new File("output/QueryLayout.html"))
  }

}

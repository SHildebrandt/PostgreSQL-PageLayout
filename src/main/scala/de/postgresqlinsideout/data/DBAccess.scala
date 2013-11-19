package de.postgresqlinsideout.data

import scala.slick.session.Database
import scala.slick.jdbc.{StaticQuery, GetResult}
import Database.threadLocalSession

/**
 *
 * @author Steffen Hildebrandt
 */
object DBAccess {

  lazy val db = Database.forURL("jdbc:postgresql://localhost/booktown", user = "postgres", password = "postgres")
  Class.forName("org.postgresql.Driver")
  // initialize PostgreSQL driver

  implicit val getPageHeaderResult = GetResult(r =>
    PageHeader(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getHeapPageItemResult = GetResult(r =>
    HeapPageItem(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, stringToIntTupel(r.<<[String]), r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getStringResult = GetResult(r => {
    var s = ""
    while (r.hasMoreColumns)
      s += r.<<[String] + "  "
    s
  })

  implicit def stringToIntTupel(s: String): (Int, Int) = s.substring(1, s.size - 1).split(",").toList match {
    case a :: b :: Nil => (a.toInt, b.toInt)
    case _ => throw new Exception("Could not parse Int-Tuple: " + s)
  }

  def getPageHeader(table: String, pageNo: Int): PageHeader = {
    db withSession {
      val result = StaticQuery.queryNA[PageHeader](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))").list()
      if (result.size != 1)
        throw new Exception("Unexpected result size of function page_header")
      result(0)
    }
  }

  def getHeapPageItems(table: String, pageNo: Int): List[HeapPageItem] = {
    db withSession {
      StaticQuery.queryNA[HeapPageItem](s"SELECT * FROM heap_page_items(get_raw_page('$table', $pageNo))").list()
    }
  }

  def getPageHeaderString(table: String, pageNo: Int): List[String] = {
    db withSession {
      //StaticQuery.queryNA[String](s"SELECT * FROM pg_tables").list()
      val q = StaticQuery.queryNA[String](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))")
      println(q.getStatement)
      q.list()
    }
  }
}

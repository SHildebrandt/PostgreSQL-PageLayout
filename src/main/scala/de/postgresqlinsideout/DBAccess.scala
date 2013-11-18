package de.postgresqlinsideout

import scala.slick.session.{Session, Database}
import scala.slick.jdbc.{StaticQuery, GetResult}
import Database.threadLocalSession
import scala.slick.jdbc.StaticQuery.interpolation

/**
 *
 *
 * @author Steffen Hildebrandt
 * @version 14.1
 */
object DBAccess {

  lazy val db = Database.forURL("jdbc:postgresql://localhost/booktown", user="steffen", password="")
  lazy val db1 = Database.forURL("jdbc:postgresql://localhost/dell", user="postgres", password="postgres")
  Class.forName("org.postgresql.Driver") // initialize driver

  case class PageHeader(lsn: String, checksum: Int, flags: Int, lower: Int, upper: Int, special: Int,
                        pagesize: Int, version: Int, pruneXid: Int)

  case class HeapPageItem(lp: Int, lpOff: Int, lpFlags: Int, lpLen: Int, tXmin: Int, tXmax: Int, tField3: Int,
                          tCtid: (Int, Int), tInfomask2: Int, tInfomask: Int, tHoff: Int, tBits: String, tOid: Int)

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

  implicit def stringToIntTupel(s: String): (Int, Int) = s.substring(1, s.size-1).split(",").toList match {
    case a::b::Nil => (a.toInt, b.toInt)
    case _ => throw new Exception("Could not parse Int-Tuple: " + s)
  }

  def getPageHeader(table: String, pageNo: Int): List[PageHeader] = {
    db withSession {
      StaticQuery.queryNA[PageHeader](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))").list()
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

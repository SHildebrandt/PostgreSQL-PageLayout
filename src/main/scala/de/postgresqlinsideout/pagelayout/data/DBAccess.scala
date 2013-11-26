package de.postgresqlinsideout.pagelayout.data

import scala.slick.session.Database
import scala.slick.jdbc.{StaticQuery, GetResult}
import Database.threadLocalSession
import scala.collection.mutable.ListBuffer

/**
 *
 * @author Steffen Hildebrandt
 */
object DBAccess {

  // initialize PostgreSQL driver
  Class.forName("org.postgresql.Driver")

  implicit val getPageHeaderResult = GetResult(r =>
    PageHeaderData(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getHeapPageItemResult = GetResult(r =>
    HeapPageItem(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, stringToIntTupel(r.<<[String]), r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getStringListResult = GetResult(r => {
    val result = ListBuffer[String]()
    while (r.hasMoreColumns)
      result += r.<<
    result.toList
  })

  implicit val getStringResult = GetResult(r => {
    var s = ""
    while (r.hasMoreColumns)
      s += r.<<[String] + "  "
    s
  })

  def stringToIntTupel(s: String): (Int, Int) = s.substring(1, s.size - 1).split(",").toList match {
    case a :: b :: Nil => (a.toInt, b.toInt)
    case _ => throw new Exception("Could not parse Int-Tuple: " + s)
  }

  def getPageHeader(db: Database, table: String, pageNo: Int): PageHeaderData = db withSession {
    val result = StaticQuery.queryNA[PageHeaderData](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))").list
    if (result.size != 1)
      throw new Exception("Unexpected result size of function page_header")
    result(0)
  }

  def getHeapPageItems(db: Database, table: String, pageNo: Int): List[HeapPageItem] = db withSession {
    val items = StaticQuery.queryNA[HeapPageItem](s"SELECT * FROM heap_page_items(get_raw_page('$table', $pageNo))").list
    items foreach { i =>
      i.fromDB = Some(db)
      i.fromTable = Some(table)
    }
    items
  }

  def getPageHeaderString(db: Database, table: String, pageNo: Int): List[String] = db withSession {
    //StaticQuery.queryNA[String](s"SELECT * FROM pg_tables").list()
    val q = StaticQuery.queryNA[String](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))")
    println(q.getStatement)
    q.list()
  }

  def getContent(db: Database, table: String): List[List[String]] = db withSession {
    StaticQuery.queryNA[List[String]](s"SELECT * FROM '$table'").list
  }

  def getContentForCtid(db: Database, table: String, ctid: (Int, Int)) = db withSession {
    StaticQuery.queryNA[List[String]](s"SELECT * FROM $table WHERE ctid = '$ctid'").list.head
  }
}

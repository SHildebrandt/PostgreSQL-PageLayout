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

  val db = Database.forURL("jdbc:postgresql://localhost/dell", user="postgres", password="postgres")

  case class PageHeader(lsn: String, checksum: Int, flags: Int, lower: Int, upper: Int, special: Int,
                        pagesize: Int, version: Int, pruneXid: Int)

  case class HeapPageItem(lp: Int, lpOff: Int, lpFlags: Int, lpLen: Int, tXmin: Int, tXmax: Int, tField3: Int,
                          tCtid: (Int, Int), tInfomask2: Int, tInfomask: Int, tHoff: Int, tBits: String, tOid: Int)

  implicit val getPageHeaderResult = GetResult(r =>
    PageHeader(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getHeapPageItemResult = GetResult(r =>
    HeapPageItem(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  def getPageHeader(table: String, pageNo: Int): List[PageHeader] = {
    db withSession {
      StaticQuery.queryNA[PageHeader](s"SELECT * FROM page_header(get_raw_page(${table}}, ${pageNo}}))").list()
    }
  }
}

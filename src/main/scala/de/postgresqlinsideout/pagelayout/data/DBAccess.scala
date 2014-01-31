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

package de.postgresqlinsideout.pagelayout.data

import scala.slick.session.Database
import scala.slick.jdbc.{StaticQuery, GetResult}
import Database.threadLocalSession
import scala.collection.mutable.ListBuffer

/**
 * This object provides all the necessary utilities for the database access
 * This includes fetching the PageHeaderData of pages, the HeapTupleItemData
 * or the content of a page as well as the corresponding ctids
 *
 * @author Steffen Hildebrandt
 */
object DBAccess {

  // initialize PostgreSQL driver
  Class.forName("org.postgresql.Driver")

  // Implicits that tell slick how to interpret/transform rows to objects (PageHeaderData etc.)
  implicit val getPageHeaderDataResult = GetResult(r =>
    PageHeaderData(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  implicit val getHeapPageItemDataResult = GetResult(r =>
    HeapPageItemData(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, r.<<, stringToIntTupel(r.<<[String]), r.<<, r.<<, r.<<, r.<<, r.<<))

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

  implicit val getCtidsResult = GetResult(r => stringToIntTupel(r.<<[String]))

  def stringToIntTupel(s: String): (Int, Int) = {
    val start = s.indexOf("(")
    val end = s.indexOf(")")
    s.substring(start + 1, end).split(",").toList match {
      case a :: b :: Nil => (a.toInt, b.toInt)
      case _ => throw new Exception("Could not parse Int-Tuple: " + s)
    }
  }

  /**
   * Takes a Database, a table and a page number and returns the PageHeaderData of that table's page in the database
   * @param db the database
   * @param table the table
   * @param pageNo the page number
   * @return The PageHeaderData
   */
  def getPageHeaderData(db: Database, table: String, pageNo: Int): PageHeaderData = db withSession {
    val result = StaticQuery.queryNA[PageHeaderData](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))").list
    if (result.size != 1)
      throw new Exception("Unexpected result size of function page_header")
    result(0)
  }

  /**
   * Takes a Database, a table and a page number and returns a list of all the HeapPageItems of that table's page in the database
   * @param db the database
   * @param table the table
   * @param pageNo the page number
   * @return A list of all the HeapPageItems
   */
  def getHeapPageItemsData(db: Database, table: String, pageNo: Int): List[HeapPageItemData] = db withSession {
    val items = StaticQuery.queryNA[HeapPageItemData](s"SELECT * FROM heap_page_items(get_raw_page('$table', $pageNo))").list
    items foreach { i =>
      i.fromDB = Some(db)
      i.fromTable = Some(table)
    }
    items
  }

  /**
   * Returns the ctids of all entries in a table on which the condition holds
   * The results can be filtered by pages. If filterPages is None, all results are returned.
   * @param db the database
   * @param table the table
   * @param condition the condition (e.g. "WHERE AUTHOR_ID = 1212")
   * @param filterPages filters the resulting ctids by the pages indicated, if it is None all ctids are returned
   * @return a list of the ctids that satisfy the condition
   */
  def getCtidsForCondition(db: Database, table: String, condition: String, filterPages: Option[Set[Int]] = None): List[(Int, Int)] = db withSession {
    val result = StaticQuery.queryNA[(Int, Int)](s"SELECT ctid FROM $table $condition").list
    filterPages match {
      case None => result
      case Some(xs) => result filter (xs contains _._1)
    }
  }

  def getContentForCtid(db: Database, table: String, ctid: (Int, Int)): List[String] = db withSession {
    StaticQuery.queryNA[List[String]](s"SELECT * FROM $table WHERE ctid = '$ctid'").list.headOption.getOrElse(List())
  }

  def getPageHeaderString(db: Database, table: String, pageNo: Int): List[String] = db withSession {
    val q = StaticQuery.queryNA[String](s"SELECT * FROM page_header(get_raw_page('$table', $pageNo))")
    println(q.getStatement)
    q.list()
  }

  def getContent(db: Database, table: String): List[List[String]] = db withSession {
    StaticQuery.queryNA[List[String]](s"SELECT * FROM '$table'").list
  }

  def getColumnNames(db: Database, table: String): List[String] = db withSession {
    val query = s"SELECT column_name FROM information_schema.columns WHERE table_name = '$table'"
    StaticQuery.queryNA[List[String]](query).list.flatten
  }
}

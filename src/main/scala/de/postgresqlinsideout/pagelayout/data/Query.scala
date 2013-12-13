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

import scala.Some
import scala.slick.session.Database
import de.postgresqlinsideout.pagelayout.visualization.{HtmlTable, PageVisualization, LayoutProperties}
import scala.util.{Success, Failure, Try}

/**
 * A PostgreSQL query on a specific page(!) and its contents.
 *
 * @author Steffen Hildebrandt
 */
class Query(override val db: Database, override val table: String, val condition: String, override val pageNo: Int)
  extends Page(db, table, pageNo) {

  private val ctids = DBAccess.getCtidsForCondition(db, table, condition, Some(Set(pageNo)))
  override protected lazy val heapPageItems = DBAccess.getHeapPageItemsData(db, table, pageNo) filter (ctids contains _.tCtid.value)

}

object Query {

  def unapply(q: Query) = Some((q.db, q.table, q.condition, q.pageNo))

  def getVisualisationsOfAllPages(db: Database, table: String, condition: String, layout: LayoutProperties): List[PageVisualization] = {
    def visOfPage(pageNo: Int): Try[PageVisualization] = {
      val q = new Query(db, table, condition, pageNo)
      Try(new HtmlTable(q, layout))
    }
    val visStream = Stream from 0 map (n => visOfPage(n))
    val visualizations = (visStream takeWhile (_.isSuccess)).toList
    visualizations map {case Success(v) => v}
  }
  
}
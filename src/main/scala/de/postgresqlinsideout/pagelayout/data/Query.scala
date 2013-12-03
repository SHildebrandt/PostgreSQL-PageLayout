package de.postgresqlinsideout.pagelayout.data

import scala.Some
import scala.slick.session.Database

/**
 * A PostgreSQL query on a specific page(!) and its contents.
 *
 * @author Steffen Hildebrandt
 */
class Query(db: Database, table: String, condition: String, pageNo: Int)
  extends Page(db, table, pageNo) {

  private val ctids = DBAccess.getCtidsForCondition(db, table, condition, Some(Set(pageNo)))
  override protected lazy val heapPageItems = DBAccess.getHeapPageItems(db, table, pageNo) filter (ctids contains _.tCtid.value)

}

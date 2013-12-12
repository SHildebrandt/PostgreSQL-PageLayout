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
class Query(db: Database, table: String, condition: String, pageNo: Int)
  extends Page(db, table, pageNo) {

  private val ctids = DBAccess.getCtidsForCondition(db, table, condition, Some(Set(pageNo)))
  override protected lazy val heapPageItems = DBAccess.getHeapPageItemsData(db, table, pageNo) filter (ctids contains _.tCtid.value)

  override def getPageVisualization(withLayout: LayoutProperties = new LayoutProperties {}): PageVisualization =
    new HtmlTable(pageElements, table, pageNo, Some(condition)) {
      override val layout = withLayout
    }
}

object Query {

  def getVisualisationsOfAllPages(db: Database, table: String, condition: String, layout: LayoutProperties): List[PageVisualization] = {
    def visOfPage(pageNo: Int): Try[PageVisualization] = {
      val q = new Query(db, table, condition, pageNo)
      Try(q.getPageVisualization(layout))
    }
    val visStream = Stream from 0 map (n => visOfPage(n))
    val visualizations = (visStream takeWhile (_.isSuccess)).toList
    visualizations map {case Success(v) => v}
  }
  
}
package de.postgresqlinsideout.pagelayout.visualization

import java.io.File
import de.postgresqlinsideout.pagelayout.data.{Query, Page}

/**
 * Trait for a page visualization.
 * So far the only implementation is a HtmlTable.
 *
 * @author Steffen Hildebrandt
 */
abstract class PageVisualization(page: Page) {

  protected val db = page.db
  protected val table = page.table
  protected val pageNo = page.pageNo

  protected val query: Option[String] = page match {
    case Query(_, _, c, _) => Some(c)
    case _ => None
  }

  protected val elements = page.pageElements

  def printToFile(to: File)

}

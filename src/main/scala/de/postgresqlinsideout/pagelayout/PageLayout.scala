package de.postgresqlinsideout.pagelayout

import de.postgresqlinsideout.pagelayout.representation.HtmlTable
import de.postgresqlinsideout.pagelayout.data.DBAccess

/**
 * This object provides utilities to create a visualization of a PostgreSQL page
 *
 * @author Steffen Hildebrandt
 */
object PageLayout {

  def getRepresentation(dbTable: String, pageNo: Int) = {

    val html = new HtmlTable
    val pageHeader = DBAccess.getPageHeader(dbTable, pageNo)
    val heapPageItems = DBAccess.getHeapPageItems(dbTable, pageNo)



  }

}

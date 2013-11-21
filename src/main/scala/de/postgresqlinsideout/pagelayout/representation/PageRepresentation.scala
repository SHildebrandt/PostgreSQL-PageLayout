package de.postgresqlinsideout.pagelayout.representation

import java.io.File

/**
 * Trait for a page representation.
 * So far the only implementation is a HtmlTable.
 *
 * @author Steffen Hildebrandt
 */
trait PageRepresentation {
  def addItem(item: PageElement)
  def printToFile(to: File)
}

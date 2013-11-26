package de.postgresqlinsideout.pagelayout.visualization

import java.io.File

/**
 * Trait for a page visualization.
 * So far the only implementation is a HtmlTable.
 *
 * @author Steffen Hildebrandt
 */
trait PageVisualization {

  def printToFile(to: File)

}

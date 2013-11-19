package de.postgresqlinsideout.pagelayout.representation

import de.postgresqlinsideout.pagelayout.representation.ContentType._

/**
 * An item within a page.
 * Objects of this class can be added to a @link{PageRepresentation}.
 *
 * @author Steffen Hildebrandt
 */
case class PageItem(firstByte: Int, lastByte: Int, contentType: ContentType, content: String)

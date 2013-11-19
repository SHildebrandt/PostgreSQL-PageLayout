package de.postgresqlinsideout.html

/**
 * @author Steffen Hildebrandt
 */
object ContentType extends Enumeration {
  case class ContentTypeVal(name: String, tdClass: String) extends Val(name)

  type ContentType = ContentTypeVal
  val DATA = ContentTypeVal("Data", "data")
  val HEADER = ContentTypeVal("Header", "header")
  val EMPTY = ContentTypeVal("Empty", "empty")
}

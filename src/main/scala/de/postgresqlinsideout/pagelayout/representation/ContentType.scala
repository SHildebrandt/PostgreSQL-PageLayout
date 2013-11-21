package de.postgresqlinsideout.pagelayout.representation

/**
 * @author Steffen Hildebrandt
 */
object ContentType extends Enumeration {
  case class ContentTypeVal(name: String, tdClass: String) extends Val(name)

  type ContentType = ContentTypeVal
  val DATA = ContentTypeVal("Data", "data")
  val PAGE_HEADER = ContentTypeVal("PageHeader", "pageheader")
  val ITEM_HEADER = ContentTypeVal("DataHeader", "dataheader")
  val ITEM_ID_DATA = ContentTypeVal("ItemIdData", "itemid")
  val EMPTY = ContentTypeVal("Empty", "empty")
}

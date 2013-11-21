package de.postgresqlinsideout.pagelayout.data

import de.postgresqlinsideout.pagelayout.representation.{ContentType, PageElement}

/**
 * Atomic field on a PostgreSQL page, e.g. in the page header or heap tuple header
 *
 * @author Steffen Hildebrandt
 */
class Field[T](val name: String, val value: T, val size: Int)  {

  override def toString = s"$name=$value"

}

abstract class FieldList {

  def toList(): List[Field[_]]

  def toTableItemList(offset: Int) =
    this.toList().foldLeft((offset, List[PageElement]()))((tuple, f) => {
      val (off, list) = tuple
      val newOff = off + f.size
      val item = PageItem(off, newOff, ContentType.PAGE_HEADER, f.value.toString)
      (newOff, list:+item)
    })._2

  def itemString = "(" + (this.toList map (f => s"${f.name}=${f.value}") mkString ",") + ")"
}

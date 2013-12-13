package de.postgresqlinsideout.pagelayout.data

/**
 * Atomic field on a PostgreSQL page, e.g. in the page header or heap tuple header
 *
 * @author Steffen Hildebrandt
 */
class Field[T](val name: String, val value: T, val size: Int) {

  override def toString = s"$name=$value"

}

abstract class FieldList {

  def toList(): List[Field[_]]

  def itemString = "(" + (this.toList map (f => s"${f.name}=${f.value}") mkString " , ") + ")"
}
